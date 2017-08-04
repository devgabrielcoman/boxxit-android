package com.boxxit.boxxit.library.parse.models.facebook;

public class Profile {

    public String id;
    public String name;
    public String birthday;
    public String email;
    public String first_name;
    public String gender;
    public Picture picture;
    public FacebookData<Object> friends = new FacebookData<>();
}
