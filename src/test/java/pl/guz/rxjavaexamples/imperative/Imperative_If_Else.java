package pl.guz.rxjavaexamples.imperative;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.Assert;
import org.junit.Test;

public class Imperative_If_Else {

    private void example() {
        User user = new User("Tomek", 20);
        User userWithChangingName = changeNameUsingAge(user);
    }

    private User changeNameUsingAge(User user) {
        Integer age = user.age();
        if (age > 20) {
            return user.copyWithNewName(user.name().toUpperCase());
        } else {
            return user.copyWithNewName(user.name().toLowerCase());
        }
    }

    private Single<User> changeNameUsingAgeRx(User oldUser) {
        Single.just(oldUser)
                .filter(user -> user.age() > 20)
                .map(user -> user.copyWithNewName(user.name().toUpperCase()))
                .switchIfEmpty(Maybe.just(oldUser.copyWithNewName(oldUser.name().toLowerCase())))
                .toSingle();

        return Single.just(oldUser)
                .map(this::changeNameUsingAge);
    }

    @Test
    public void should_return_user_with_upper_case_name() {
        User user = new User("Tomek", 21);
        User userWithChangingName = changeNameUsingAge(user);

        Assert.assertEquals(userWithChangingName.name(), "TOMEK");
    }

    @Test
    public void should_return_user_with_lower_case_name() {
        User user = new User("Tomek", 19);
        User userWithChangingName = changeNameUsingAge(user);

        Assert.assertEquals(userWithChangingName.name(), "tomek");
    }

    @Test
    public void should_return_user_with_upper_case_name_rx() {
        User user = new User("Tomek", 21);
        changeNameUsingAgeRx(user)
                .map(User::name)
                .test()
                .assertValue("TOMEK");

    }

    @Test
    public void should_return_user_with_lower_case_name_rx() {
        User user = new User("Tomek", 19);
        changeNameUsingAgeRx(user)
                .map(User::name)
                .test()
                .assertValue("tomek");
    }
}


class User {
    private final String name;
    private final Integer age;

    User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    String name() {
        return name;
    }

    Integer age() {
        return age;
    }

    User copyWithNewName(String newName) {
        return new User(newName, age);
    }
}