package com.example.qas_backend.post.aspect;

import java.lang.annotation.*;

/**
 * 需要用户身份
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserRequired {
}
