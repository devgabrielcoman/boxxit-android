package com.boxxit.boxxit.library.base;

public interface Task <Input, Output, Result> {
    Result execute(Input input);
}
