package com.ucbcba.joel.ucbcorreccionformato;

import java.io.IOException;

import javax.security.auth.message.callback.PrivateKeyCallback.Request;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class MyFIlter implements Filter {

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
            throws IOException, ServletException {
        HttpServletRequest requ = (HttpServletRequest) arg0;
        if (requ.getMethod().toLowerCase().equals(HttpMethod.OPTIONS)){
            arg2.doFilter(arg0, arg1);
            return;
        }
        arg2.doFilter(arg0, arg1);
        
    }
}
