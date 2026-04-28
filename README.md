# Template API CURD Service

This project is a template for creating a CRUD (Create, Read, Update, Delete) service using Spring Boot. It provides a basic structure and implementation for building RESTful APIs with Spring Boot.

Refer to `https://www.xuxiaoye.com/legacy/misc/crudTemplate` for creating entity classes.

Will update detail here when I have spare time.

## Open API Generator

## Spring contract

## Authorization

### Authorization object matrix
|        | Create | Search Any | Search Own | Get Any | Get Own | Delete Any | Delete Own | Export | Export Own | Import | Import Own |
|--------|--------|------------|------------|---------|---------|------------|------------|--------|------------|--------|------------|
| Admin  | X      | X          | X          | X       | X       | X          | X          | X      | X          | X      | X          |
| Member | X      | X          | X          | X       | X       |            |            | X      | X          | X      | X          |
| Owner  | X      |            | X          |         | X       |            | X          |        | X          |        | X          |
| Guest  |        |            |            |         |         |            |            |        |            |        |            |

### Authorization config matrix

User -> Profile -> Role -> Permission
User -> Role
User -> Permission

| User  | Profile       | Role       | Authority      | 
|-------|---------------|------------|----------------|
| user1 | MemberProfile |            |                | 
| user1 | DevOpsProfile |            |                | 
| user2 |               | MemberRole |                | 
| user2 |               | GuestRole  |                | 
| user3 |               |            | student:search | 
| user3 |               |            | student:get    | 


| Profile       | Role             |
|---------------|------------------|
| MemberProfile | MemberViewRole   |
| MemberProfile | MemberUpdateRole |
| DevOpsProfile | DevOpsRole       |
| DevOpsProfile | AdminRole        |
| AdminProfile  | AdminRole        |


| Role               | Authority          |
|--------------------|--------------------|
| AdminRole          | student:search     |
| AdminRole          | student:search_own |
| AdminRole          | student:get        |
| AdminRole          | student:get_own    |
| AdminRole          | student:update     |
| AdminRole          | student:update_own |
| AdminRole          | student:delete     |
| AdminRole          | student:delete_own |
| AdminRole          | student:create     |
| AdminRole          | student:export     |
| AdminRole          | student:export_own |
| AdminRole          | student:import     |
| AdminRole          | student:import_own |
| MemberViewRole     | student:search     |
| MemberViewRole     | student:get        |
| MemberUpdateRole   | student:update     |

