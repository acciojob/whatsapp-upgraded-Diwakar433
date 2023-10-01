package com.driver;

import java.util.*;

public class WhatsappRepository {

    HashMap<String, User> userDB = new HashMap<>();
    int countOfGroups = 0;

    HashMap<Group,List<User>> groupUsersDb = new HashMap<>();
    HashMap<Group,User> groupAdminDB = new HashMap<>();
    HashMap<Integer,Message> messageHashMap = new HashMap<>();
    HashMap<Group,List<Message>> groupToMessagesDb = new HashMap<>();

    HashMap<Message,User> senderMap = new HashMap<>();

    public String createUser(String name, String mobile) throws Exception {
        try {
            if(userDB.containsKey(mobile)) {
                throw new UserExistException("User already exists");
            }

            // Set the data using setter and save to userDB
            User user = new User();
            user.setName(mobile);
            user.setName(name);

            userDB.put(mobile, user);

            return "SUCCESS";
        }
        catch (Exception e) {
            return e.getMessage();
        }
        

    }

    public Group createGroup(List<User> users) {

        if (users.size() == 2) {
            Group group = new Group(users.get(1).getName(),2);
            groupAdminDB.put(group,users.get(0));
            groupUsersDb.put(group,users);
            groupToMessagesDb.put(group,new ArrayList<>());
            return group;
        }
        else {
            this.countOfGroups++;
            Group group = new Group("Group " + countOfGroups,users.size());
            groupAdminDB.put(group,users.get(0));
            groupUsersDb.put(group,users);
            groupToMessagesDb.put(group,new ArrayList<>());
            return group;
        }
    }

    public int createMessage(String content) {
        int size = messageHashMap.size();

        Message message = new Message(size+1, content, new Date());

        messageHashMap.put(size+1, message);

        return size+1;
    }

    public int sendMessage(Message message, User sender, Group group) {
        // if group is not exist then return exception
        if(groupUsersDb.containsKey(group))
            throw new RuntimeException("Group does not exist");

        // check sender is not exist then return exception
        List<User> users = groupUsersDb.get(group);

        if(!users.contains(sender))
            throw new RuntimeException("You are not allowed to send message");

        List<Message> messageList = groupToMessagesDb.get(group);
        messageList.add(message);
        groupToMessagesDb.put(group, messageList);
        senderMap.put(message, sender);

        return groupToMessagesDb.get(group).size();

    }

    public String changeAdmin(User approver, User user, Group group) {
        // if group does not exist then
        if(groupUsersDb.containsKey(group))
            throw new RuntimeException("Group does not exist");

        // if approver does not exist
        if(groupAdminDB.get(group) != approver)
            throw new RuntimeException("Approver does not have rights");

        // if user is not part of group then return exception

        List<User> userList = groupUsersDb.get(group);
        if(!userList.contains(user))
            throw new RuntimeException("User is not a participant");

        // change the admin of group and change all required db
        groupAdminDB.remove(group);
        groupAdminDB.put(group, user);

        return "SUCCESS";
    }

    public int removeUser(User user) {
        Boolean flag = false;

        List<Message> messageList = new ArrayList<>();
        for (Message message : senderMap.keySet()){
            if (senderMap.get(message).equals(user)){
                messageList.add(message);
            }
        }
        for (Message message : messageList){
            if (senderMap.containsKey(message)){
                senderMap.remove(message);
            }
        }

        Group userGroup = null;

        for(Group group : groupUsersDb.keySet()){

            if(groupAdminDB.get(group).equals(user)){
                throw new RuntimeException("Cannot remove admin");
            }
            List<User> userList = groupUsersDb.get(group);
            if(userList.contains(user)){
                flag = true;
                userGroup = group;
                userList.remove(user);

            }
        }
        if (!flag){
            throw new RuntimeException("User not found");
        }

        List<Message> messages = groupToMessagesDb.get(userGroup);
        for (Message message : messageList){
            if (messages.contains(message)){
                messages.remove(message);
            }
        }

        return groupUsersDb.get(userGroup).size() + groupToMessagesDb.get(userGroup).size() + senderMap.size();
    }

    public String findMessage(Date start, Date end, int k) {
        //This is a bonus problem and does not contain any marks
        // Find the Kth the latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception

        List<Message> messageList = new ArrayList<>();

        for (Message message : senderMap.keySet()){
            Date time = message.getTimestamp();
            if (start.before(time) && end.after(time)){
                messageList.add(message);
            }
        }
        if (messageList.size() < k){
            throw  new RuntimeException("K is greater than the number of messages");
        }

        Map<Date , Message> hm = new HashMap<>();

        for (Message message : messageList){
            hm.put(message.getTimestamp(),message);
        }

        List<Date> dateList = new ArrayList<>(hm.keySet());

        Collections.sort(dateList, new sortCompare());

        Date date = dateList.get(k-1);

        return hm.get(date).getContent();

    }

    static class sortCompare implements Comparator<Date>
    {
        @Override
        // Method of this class
        public int compare(Date a, Date b)
        {
            /* Returns sorted data in Descending order */
            return b.compareTo(a);
        }
    }
}
