package com.boxxit.boxxit.library.auth;

import com.boxxit.boxxit.library.base.Request;

import java.util.Arrays;
import java.util.Collection;

public class FacebookAuthRequest implements Request {

    final Collection<String> permissions = Arrays.asList(
            "public_profile",
            "email",
            "user_friends",
            "user_likes",
            "user_birthday");

}
