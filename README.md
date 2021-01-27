
# rubduk API
## User API
### GET api/users
**Parameters:**
- offset: *int*
- limit: *int*
- name: *string* (optional)

**Returns:** Page of *User* JSON objects.
***

### GET api/users/{userId}
**Returns:** *User* JSON object with the specified ID or a 404 error with an explanatory message.
***

### GET api/users/{userId}/media
**Parameters:**
- offset: *int*
- limit: *int*

**Returns:** Page of *Media* JSON objects that belong to the user with the specified ID.
***
### POST api/users/login
**Entity**: *IdToken* JSON

**Returns:** The logged-in/newly registered *User* JSON object or fails
if the token is invalid with a 400 status code.
***

### POST api/users/media
**Headers:** *Authorization* with an *IdToken*
**Entity:** *ImageRequest* JSON
**Description:** Inserts a new medium associated with the user.

**Returns:** *MediumId* of the inserted image.
***

### PUT api/users/{userId}
**Headers:** *Authorization* with an *IdToken*
**Entity:** *UserRequest* JSON
**Description:** Updates your profile.

**Returns:** Just a 200 status code if everything went smoothly or an explanatory error message  
if there were errors along the way.
***

### PUT api/users/me/pic
**Headers:** *Authorization* with an *IdToken*
**Entity:** *MediumId*
**Description:** Updates your profile picture with the medium whose ID you specify in the entity.

**Returns:** Just a 200 status code if everything went smoothly or an explanatory error message  
if there were errors along the way.


## Post API
### GET api/posts
**Parameters:**
- offset: *int*
- limit: *int*
- userId: *long* (optional)

**Returns:** Page of *Post* JSON objects or conditionally, posts of a user with the specified ID.
***

### GET api/posts/{postId}
**Returns:** *Post* JSON object with the specified ID if one exists, 404 status with a message otherwise.
***

### GET api/posts/{postId}/comments
**Parameters:**
- offset: *int*
- limit: *int*

**Returns:** Page of *Comment* JSON objects associated with a post with the specified ID,
404 status code with an explanatory message if a post with the ID doesn't exist.
 ***

### GET api/posts/{postId}/comments/{commentId}
**Returns:** *Comment* JSON object with the specified ID if one exists, 404 status with a message otherwise.
 ***
### POST api/posts
**Headers:** *Authorization* with an *IdToken*
**Entity:** *PostRequest* JSON
**Description:** Inserts the sent post and assigns it to the user.

**Returns:** *PostId* of the inserted post.
***

### POST api/posts/{postId}/comments
**Headers:** *Authorization* with an *IdToken*
**Entity:** *CommentRequest* JSON
**Description:** Inserts the sent comment and assigns it to the user.

**Returns:** *CommentId* of the inserted comment.
***

### PUT api/posts/{postId}/like
**Headers:** *Authorization* with an *IdToken*
**Description:** Increases the like count of a post with the specified id.

**Returns:** Just a 200 status code if everything went smoothly, 422 if
the user has already liked that post.
***

### PUT api/posts/{postId}/unlike
**Headers:** *Authorization* with an *IdToken*
**Description:** Decreases the like count of a post with the specified id.

**Returns:** Just a 200 status code if everything went smoothly, 422 if
the user hasn't already liked that post.
***

### PUT api/posts/{postId}
**Headers:** *Authorization* with an *IdToken*
**Entity:** *PostRequest* JSON
**Description:** Updates the given post.

**Returns:** Just a 200 status code if everything went smoothly.
***

### PUT api/posts/{postId}/comments/{commentId}
**Headers:** *Authorization* with an *IdToken*
**Entity:** *CommentRequest* JSON
**Description:** Updates the given comment.

**Returns:** Just a 200 status code if everything went smoothly.
***

## Friend Request API
### POST api/friends
**Headers:** *Authorization* with an *IdToken*
**Entity:** *FriendRequestRequest* JSON
**Description:** Sends a friend request to the specified user.

**Returns:** *FriendRequestId* of the newly sent request or an error with an explanatory
message otherwise.
***
### GET api/friends
**Headers:** *Authorization* with an *IdToken*
**Parameters:**
- offset: *int*
- limit: *int*

**Returns:** Page of accepted *FriendRequest* JSON objects of the logged in user.
 ***

### GET api/friends/pending
**Headers:** *Authorization* with an *IdToken*
**Parameters:**
- offset: *int*
- limit: *int*

**Returns:** Page of pending *FriendRequest* JSON objects sent to the logged in user.
 ***
### GET api/friends/pending/sent
**Headers:** *Authorization* with an *IdToken*
**Parameters:**
- offset: *int*
- limit: *int*

**Returns:** Page of pending *FriendRequest* JSON objects sent by the logged in.
 ***
### POST api/friends/accept/{friendRequestId}
**Headers:** *Authorization* with an *IdToken*
**Description:** Accepts an incoming friend request.

**Returns:** Just a 200 status code if everything went smoothly.
***

### POST api/friends/reject/{friendRequestId}
**Headers:** *Authorization* with an *IdToken*
**Description:** Rejects an incoming friend request.

**Returns:** Just a 200 status code if everything went smoothly.
***

## Feed API
### GET api/feed/posts
**Headers:** *Authorization* with an *IdToken*
**Parameters:**
- offset: *int*
- limit: *int*

**Returns:** Page of *Post* JSON objects by user's friends and himself.
***
### GET api/feed/media
**Headers:** *Authorization* with an *IdToken*
**Parameters:**
- offset: *int*
- limit: *int*

**Returns:** Page of *Media* JSON objects by user's friends and himself.
***


### Running Flyway migrations
Execute `flyway.sh` (you may want to add execute rights to the file first with
 `chmod +x flyway.sh`).  
 
 The script accepts these optional parameters:  
 `-n <username>` - database username (`postgres` by default),   
 `-p <password>` - database password (`postgres` by default),  
 `-u <URL>` - JDBC database url `jdbc:postgresql://localhost:5432/rubduk` by default),   
 `-d <name>` - JDBC database name on default url `jdbc:postgresql://localhost:5432/<name>` by default),   
 `-c` - run `flywayClean` instead of `flywayMigrate`.
 
 
