package com.example.userservice.repository;

import com.example.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 * 继承JpaRepository提供基础CRUD操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户（登录使用）
     *
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsernameAndIsDeleted(String username, Integer isDeleted);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmailAndIsDeleted(String email, Integer isDeleted);

    /**
     * 根据手机号查找用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhoneAndIsDeleted(String phone, Integer isDeleted);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsernameAndIsDeleted(String username, Integer isDeleted);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmailAndIsDeleted(String email, Integer isDeleted);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhoneAndIsDeleted(String phone, Integer isDeleted);

    /**
     * 分页查询未删除的用户
     *
     * @param pageable 分页参数
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = 0")
    Page<User> findAllActiveUsers(Pageable pageable);

    /**
     * 根据用户ID查找未删除的用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.isDeleted = 0")
    Optional<User> findActiveUserById(@Param("userId") Long userId);

    /**
     * 模糊查询用户名
     *
     * @param username 用户名关键字
     * @param pageable 分页参数
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username% AND u.isDeleted = 0")
    Page<User> findByUsernameContaining(@Param("username") String username, Pageable pageable);
}