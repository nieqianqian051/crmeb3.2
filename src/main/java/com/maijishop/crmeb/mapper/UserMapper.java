package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maijishop.crmeb.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM tb_user WHERE username = #{username} AND is_del = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM tb_user WHERE phone = #{phone} AND is_del = 0")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据OpenID查询用户
     */
    @Select("SELECT * FROM tb_user WHERE openid = #{openid} AND is_del = 0")
    User selectByOpenid(@Param("openid") String openid);
} 