package balint.lenart.utils;

import balint.lenart.model.User;

public class UserUtils {

    public static Tuple<String, String> getNameByUser(User user) {
        if( user.getFullName() == null ) {
            return new Tuple<>(null, null);
        }
        if(!user.getFullName().contains(" ")) {
            return new Tuple<>(user.getFullName().trim(), null);
        }
        String firstName = user.getFullName().substring(0, user.getFullName().indexOf(" "));
        String lastName = user.getFullName().substring(user.getFullName().indexOf(" "));
        return new Tuple<>(firstName.trim(), lastName.trim());
    }

}
