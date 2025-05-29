package com.jcommsarray.signaling.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.Principal;

public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String name = ((HttpServletRequest) servletRequest).getHeader("name");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken((Principal) () -> name, null));
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
