 return userQuery.then(userQueryResult => {

    const userName = userQueryResult.val();
    console.log('You have a friend request from : ', userName);

	

	return deviceToken.then(result => {
  	
  	const token_id = result.val();

    const payload = {
	notification: {
					title : "Friend Request",
					body  : `${userName} has send you a freind request`,
					icon  : "default",
					click_action : "com.example.chatapp_TARGET_NOTIFICATION"
	},
	data : {
		from_user_id : from_user_id
	}
};

	return admin.messaging().sendToDevice(token_id, payload).then(response => {
	console.log('This was a notification feature');
});
});

     });