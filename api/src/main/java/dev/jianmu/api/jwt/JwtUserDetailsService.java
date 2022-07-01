package dev.jianmu.api.jwt;

import dev.jianmu.api.oauth2_api.config.OAuth2Properties;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.user.aggregate.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author huangxi
 * @class JwtUserDetailsService
 * @description JwtUserDetailsService
 * @create 2022-06-30 15:06
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final OAuth2Properties oAuth2Properties;
    private final JwtProperties jwtProperties;

    public JwtUserDetailsService(OAuth2Properties oAuth2Properties, JwtProperties jwtProperties) {
        this.oAuth2Properties = oAuth2Properties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String userJson) throws UsernameNotFoundException {
        User user = JsonUtil.stringToJson(userJson, User.class);
        return JwtUserDetails.build(user, this.jwtProperties.getEncryptedPassword(this.oAuth2Properties.getClientSecret()));
    }
}
