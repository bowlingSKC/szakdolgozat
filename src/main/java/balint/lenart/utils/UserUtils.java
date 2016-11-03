package balint.lenart.utils;

import balint.lenart.model.User;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class UserUtils {

    private static Map<String, List<String>> userGroups = Maps.newHashMap();

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

    public static String getGroupByEmail(String email) {
        for(String groupName : userGroups.keySet()) {
            for(String member : userGroups.get(groupName)) {
                if( member.equals(email) ) {
                    return groupName;
                }
            }
        }

        return null;
    }

}
