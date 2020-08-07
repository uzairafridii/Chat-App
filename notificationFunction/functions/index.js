'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


exports.sendNotification = functions.database.ref('/notification/{userId}/{notification_id}').onWrite((data , context) =>{

	const userId = context.params.userId;
	const notification_id = context.params.notification_id;
	console.log('We have a notification to send to ', userId);

	if(!data.after.val())
	{
		 console.log('A notification has been deleted from database ',notification_id);
		 return null;
	}


   	const fromUser = admin.database().ref(`/notification/${userId}/${notification_id}`).once('value');
   	return fromUser.then(fromUserResult => {
    const from_user_id  = fromUserResult.val().from;
    console.log('You have new notification from ', from_user_id);


    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${userId}/device_token`).once('value');


   return Promise.all([userQuery , deviceToken]).then(result => {

      const userName  = result[0].val();
      const token_id = result[1].val();

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

});


