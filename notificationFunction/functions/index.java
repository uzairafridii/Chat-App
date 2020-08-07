friendRequestDatabase.child(currentUser).child(userId).child("request_state")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                friendRequestDatabase.child(userId).child(currentUser).child("request_state")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", currentUser);
                                        notificationData.put("type", "request");

                                        notificationDatabase.child(userId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                sendFriendReqBtn.setEnabled(true);
                                                sendFriendReqBtn.setText("Cancel Friend Request");
                                                currentState = "req_sent";

                                                declineFriendReqBtn.setVisibility(View.INVISIBLE);
                                                declineFriendReqBtn.setEnabled(false);
                                            }
                                        });




                                    }
                                });
                            }
                        }
                    });