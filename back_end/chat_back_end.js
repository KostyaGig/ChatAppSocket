const express = require('express'); //requires express module
const socket = require('socket.io'); //requires socket.io module
const crypto = require('crypto'); // for uuid

const fs = require('fs');
const app = express();

var PORT = process.env.PORT || 3000;
const server = app.listen(PORT);

app.use(express.static('public'));
console.log('Server zis running');
const io = socket(server);


var userId = 0;
var users = [];
var messages = [];

var offlineUserNotificationTokens = [];

var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/";

// base uri - http://10.0.2.2:3000

io.on('connection', (socket) => {

    console.log("connection " + socket.id)

    socket.on('join_user', async (clientUser) => {

        var nickName = clientUser['nickname']
        var image = clientUser['image']

        if (nickName == '') {
        	io.emit('join_user', -1)
        } else {
        	MongoClient.connect(url, async function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");
            var users = database.collection("user")

            var query = {
                userNickName: nickName
            }

            var user = await users.findOne(query)

            if (user != null) {
                var userId = user['userId']
                io.emit('join_user', userId)
            } else {
                var count = users.count({}, function (error, count) {
                    if (error) return 0;

                    var userId = count + 1
                    console.log(userId, nickName)

                    var user = {
                        userId: userId,
                        userNickName: nickName,
                        userImage: image
                    }

                    database.collection("user").insertOne(user, function (err, res) {
                        if (err) throw err;

                        io.emit('join_user', userId)
                    });
                });
            }
            db.close();
        })
        }

    })

    socket.on('users', async (user) => {
        var userId = user['userId']
        MongoClient.connect(url, function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");
            database.collection("user").find({}).toArray(async function (err, result) {
                if (err) throw err;

                var messagesCollection = database.collection("messages")

                var users = []
                for (const currentUser of result) {
                    var user = Object()
                    var currentUserId = currentUser['userId'].toString();
                    user.id = currentUserId
                    user.nickName = currentUser['userNickName']
                    user.image = currentUser['userImage']

                    var firstMessagesJson = {
                        senderId: userId,
                        receiverId: currentUserId
                    }

                    var secondMessagesJson = {
                        senderId: currentUserId,
                        receiverId: userId
                    }
                    var firstResult = await messagesCollection.findOne(firstMessagesJson)
                    var secondResult = await messagesCollection.findOne(secondMessagesJson)

                    if (firstResult == null && secondResult != null) {
                        var messages = secondResult['messages']
                        if (messages.length != 0) {
                            var theLastMessage = messages[messages.length - 1]
                            var content = theLastMessage['content']
                            var senderNickName = theLastMessage['senderNickName']
                            var senderId = theLastMessage['senderId']

                            user.lastMessage = content
                            user.lastMessageSenderNickName = senderNickName
                        }
                    } else {
                        if (firstResult != null) {
                            var messages = firstResult['messages']
                            if (messages.length != 0) {
                                var theLastMessage = messages[messages.length - 1]
                                var content = theLastMessage['content']
                                var senderNickName = theLastMessage['senderNickName']
                                var senderId = theLastMessage['senderId']

                                user.lastMessage = content
                                user.lastMessageSenderNickName = senderNickName
                            }
                        }
                    }
                    if (user.id != userId) {
                        users.push(user)
                    }
                }
                io.emit('users', users)
                db.close();
            });
        })
    })

    socket.on('send_message', async (clientMessage) => {
        var senderId = clientMessage.senderId;
        var receiverId = clientMessage.receiverId;

        var firstQuery = {
            senderId: senderId,
            receiverId: receiverId
        }

        var secondQuery = {
            senderId: receiverId,
            receiverId: senderId
        }

        var newMessage = Object();
        var messageId = crypto.randomUUID();

        newMessage.id = messageId;
        newMessage.senderId = senderId;
        newMessage.content = clientMessage.content;
        newMessage.isRead = false;
        newMessage.senderNickName = clientMessage.senderNickName;


        MongoClient.connect(url, async function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");

            var messagesCollection = database.collection("messages")

            var firstResult = await messagesCollection.findOne(firstQuery)
            var secondResult = await messagesCollection.findOne(secondQuery)

            console.log('msg', firstResult, secondResult)

            if (firstResult != null) {
                // update messages
                var messages = firstResult['messages']

                var listOfMessages = []

                messages.forEach(function (item, index, array) {
                    var message = Object()
                    message.id = item.id
                    message.senderId = item.senderId
                    message.content = item.content
                    message.senderNickName = item.senderNickName
                    message.isRead = item.isRead

                    listOfMessages.push(message)
                })

                listOfMessages.push(newMessage)

                var jsonMessages = {
                    messages: listOfMessages
                }

                var newMessages = {$set: jsonMessages}

                io.emit('messages', listOfMessages)
                messagesCollection.updateOne(firstQuery, newMessages)
            } else {
                if (secondResult != null) {
                    var messages = secondResult['messages']

                    var listOfMessages = []

                    messages.forEach(function (item, index, array) {
                        var message = Object()
                        message.id = item.id
                        message.senderId = item.senderId
                        message.content = item.content
                        message.senderNickName = item.senderNickName
                        message.isRead = item.isRead

                        listOfMessages.push(message)
                    })

                    listOfMessages.push(newMessage)

                    var jsonMessages = {
                        messages: listOfMessages
                    }

                    var newMessages = {$set: jsonMessages}

                    io.emit('messages', listOfMessages)
                    messagesCollection.updateOne(secondQuery, newMessages)
                } else {

                    console.log('create branch')
                    var listOfMessages = []
                    listOfMessages.push(newMessage)

                    var json = {
                        senderId: senderId,
                        receiverId: receiverId,
                        messages: listOfMessages
                    }

                    io.emit('messages', listOfMessages)
                    messagesCollection.insertOne(json)
                }

            }
        })
        sendNotification(newMessage.id, newMessage.senderNickName, newMessage.content)
    })

    const functions = require("firebase-functions");
    var admin = require("firebase-admin");
    var serviceAccount = require("/Users/kostazinovev/Desktop/chat_firebase_key.json");

    if (!admin.apps.length) {
        admin.initializeApp({
            credential: admin.credential.cert(serviceAccount)
        });
    }

    function sendNotification(messageId, senderNickName, content) {


        offlineUserNotificationTokens.forEach(function (item, index, array) {
            var notificationToken = item

            const message = {
                data: {
                    messageId: messageId,
                    nickName: senderNickName,
                    content: content
                },
                token: notificationToken
            };

            admin.messaging().send(message)
                .then((response) => {
                    console.log('Successfully sent message:', response);
                })
                .catch((error) => {
                    console.log('Error sending message:', error);
                });
        })

    }


    socket.on('messages', async (data) => {
        var senderId = data['senderId']
        var receiverId = data['receiverId']

        var firstQuery = {
            senderId: senderId,
            receiverId: receiverId
        }

        var secondQuery = {
            senderId: receiverId,
            receiverId: senderId
        }

        var listOfMessages = []

        MongoClient.connect(url, async function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");
            var messagesCollection = database.collection("messages")

            var firstResult = await messagesCollection.findOne(firstQuery)
            var secondResult = await messagesCollection.findOne(secondQuery)

            if (firstResult != null) {

                var messages = firstResult['messages']

                messages.forEach(function (item, index, array) {
                    var message = Object();

                    message.id = item.id;
                    message.senderId = item.senderId;
                    message.content = item.content;
                    message.isRead = item.isRead;
                    message.senderNickName = item.senderNickName;

                    listOfMessages.push(message)
                })

            } else {
                if (secondResult != null) {

                    var messages = secondResult['messages']

                    messages.forEach(function (item, index, array) {
                        var message = Object();

                        message.id = item.id;
                        message.senderId = item.senderId;
                        message.content = item.content;
                        message.isRead = item.isRead;
                        message.senderNickName = item.senderNickName;

                        listOfMessages.push(message)
                    })
                }
            }
            io.emit("messages", listOfMessages)
            db.close();
        });
    })

    socket.on('edit_message', async (clientMessage) => {
        var messageId = clientMessage.id
        var content = clientMessage.content

        var senderId = clientMessage.senderId
        var receiverId = clientMessage.receiverId

        var firstQuery = {
            senderId: senderId,
            receiverId: receiverId
        }

        var secondQuery = {
            senderId: receiverId,
            receiverId: senderId
        }


        MongoClient.connect(url, async function (err, db) {
            var database = db.db("chat_app_db");
            var messagesCollection = database.collection("messages")

            var firstResult = await messagesCollection.findOne(firstQuery)
            var secondResult = await messagesCollection.findOne(secondQuery)

            var listOfMessages = []

            if (firstResult != null) {
                var messages = firstResult['messages']

                messages.forEach(function (item, index, array) {
                    var message = Object();
                    var editedMessageContent = item.content

                    if (messageId == item.id) {
                        editedMessageContent = content
                    }

                    message.id = item.id
                    message.senderId = item.senderId;
                    message.content = editedMessageContent;
                    message.isRead = item.isRead;
                    message.senderNickName = item.senderNickName;

                    listOfMessages.push(message)
                })

                var jsonMessages = {
                    messages: listOfMessages
                }

                var newMessages = {$set: jsonMessages}

                messagesCollection.updateOne(firstQuery, newMessages, function (err, res) {
                    if (err) throw err;

                    io.emit('messages', listOfMessages)
                    db.close();
                });
            }

            if (secondResult != null) {
                var messages = secondResult['messages']

                messages.forEach(function (item, index, array) {
                    var message = Object();
                    var editedMessageContent = item.content

                    if (messageId == item.id) {
                        editedMessageContent = content
                    }

                    message.id = item.id
                    message.senderId = item.senderId;
                    message.content = editedMessageContent;
                    message.isRead = item.isRead;
                    message.senderNickName = item.senderNickName;

                    listOfMessages.push(message)
                })

                var jsonMessages = {
                    messages: listOfMessages
                }

                var newMessages = {$set: jsonMessages}

                messagesCollection.updateOne(secondQuery, newMessages, function (err, res) {
                    if (err) throw err;

                    io.emit('messages', listOfMessages)
                    db.close();
                });
            }
        })

    })

    socket.on('read_message', async (data) => {
        var jsonIds = JSON.parse(data.ids)['ids']
        var senderId = data.senderId
        var receiverId = data.receiverId

        if (typeof jsonIds !== 'undefined') {

            var firstQuery = {
                senderId: senderId,
                receiverId: receiverId
            }

            var secondQuery = {
                senderId: receiverId,
                receiverId: senderId
            }

            MongoClient.connect(url, async function (err, db) {
                var database = db.db("chat_app_db");
                var messagesCollection = database.collection("messages")

                var firstResult = await messagesCollection.findOne(firstQuery)
                var secondResult = await messagesCollection.findOne(secondQuery)

                if (firstResult != null) {

                    var listOfReadMessages = []
                    var listOfSenderNameMessage = []
                    var messages = firstResult['messages']

                    jsonIds.forEach(function (jsonMessageId, index, array) {
                        messages.forEach(function (message, index, array) {

                            listOfSenderNameMessage.push(message.senderNickName)


                            if (message.id == jsonMessageId && message.isRead != true) {
                                var readMessage = Object()

                                readMessage.id = message.id
                                readMessage.senderId = message.senderId;
                                readMessage.content = message.content;
                                readMessage.isRead = true
                                readMessage.senderNickName = message.senderNickName;

                                listOfReadMessages.push(readMessage)
                            }
                        })


                        var willBeReadMessagesIndexes = []
                        var lastReadMessage = listOfReadMessages[listOfReadMessages.length - 1]

                        if (typeof lastReadMessage !== 'undefined') {
                            var isNotFinish = true
                            messages.forEach(function (message, index, array) {
                                if (isNotFinish) {
                                    if (message.senderNickName == lastReadMessage.senderNickName && message.isRead != true) {
                                        if (message.id != lastReadMessage.id) {
                                            willBeReadMessagesIndexes.push(index)
                                        } else {
                                            willBeReadMessagesIndexes.push(index)
                                            isNotFinish = false
                                        }
                                    }
                                }
                            })

                            willBeReadMessagesIndexes.forEach(function (value) {
                                var readMessage = messages[value]
                                readMessage.isRead = true
                                messages[value] = readMessage
                            })
                        }

                        io.emit('messages', messages)

                        var jsonMessages = {
                            messages: messages
                        }

                        var newMessages = { $set: jsonMessages}

                        messagesCollection.updateOne(secondQuery,newMessages);
                    })

                }


                if (secondResult != null) {

                    var listOfReadMessages = []
                    var listOfSenderNameMessage = []
                    var messages = secondResult['messages']

                    jsonIds.forEach(function (jsonMessageId, index, array) {
                        messages.forEach(function (message, index, array) {

                            listOfSenderNameMessage.push(message.senderNickName)


                            if (message.id == jsonMessageId && message.isRead != true) {
                                var readMessage = Object()

                                readMessage.id = message.id
                                readMessage.senderId = message.senderId;
                                readMessage.content = message.content;
                                readMessage.isRead = true
                                readMessage.senderNickName = message.senderNickName;

                                listOfReadMessages.push(readMessage)
                            }
                        })


                        var willBeReadMessagesIndexes = []
                        var lastReadMessage = listOfReadMessages[listOfReadMessages.length - 1]

                        if (typeof lastReadMessage !== 'undefined') {
                            var isNotFinish = true
                            messages.forEach(function (message, index, array) {
                                if (isNotFinish) {
                                    if (message.senderNickName == lastReadMessage.senderNickName && message.isRead != true) {
                                        if (message.id != lastReadMessage.id) {
                                            willBeReadMessagesIndexes.push(index)
                                        } else {
                                            willBeReadMessagesIndexes.push(index)
                                            isNotFinish = false
                                        }
                                    }
                                }
                            })

                            willBeReadMessagesIndexes.forEach(function (value) {
                                var readMessage = messages[value]
                                readMessage.isRead = true
                                messages[value] = readMessage
                            })
                        }

                        io.emit('messages', messages)

                        var jsonMessages = {
                            messages: messages
                        }

                        var newMessages = { $set: jsonMessages}

                        messagesCollection.updateOne(secondQuery,newMessages);
                    })

                }
            })

        }
    })


    socket.on('disconnect_user', (notificationToken) => {

        var token = notificationToken['notification_token']
        offlineUserNotificationTokens.push(token)

        console.log('disconnectUser', offlineUserNotificationTokens)
    })

    socket.on('connect_user', (notificationToken) => {
        var token = notificationToken['notification_token']
        var indexOf = offlineUserNotificationTokens.indexOf(token)

        if (indexOf > -1) {
            offlineUserNotificationTokens.splice(indexOf, 1)
        }

        console.log('connectUser', offlineUserNotificationTokens)
    })


    var isTypingYet = false

    socket.on('to_type_message', (message) => {
        var objectMessage = Object()
        var isTyping = message['isTyping']
        var senderNickName = message['senderNickName']

        objectMessage.senderNickName = senderNickName

        if (isTyping) {
            if (isTypingYet == false) {
                objectMessage.isTyping = true
                isTypingYet = true
                io.emit('to_type_message', objectMessage)
                console.log('push', objectMessage)
            }
        } else {
            if (isTypingYet == true) {
                objectMessage.isTyping = false
                isTypingYet = false
                io.emit('to_type_messaage', objectMessage)
                console.log('push', objectMessage)
            }
        }
    })


    var MongoClient = require('mongodb').MongoClient;
    var url = "mongodb://localhost:27017/";


    socket.on('testAddSenderIdAndReceiverId', (user) => {
        console.log(user)
        // var jsonUser = JSON.parse(user)
        // console.log(jsonUser)
        // console.log(user['senderId'],user['receiverId'])

        var senderId = user['senderId']
        var receiverId = user['receiverId']

        MongoClient.connect(url, function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");
            var user = {
                senderId: senderId,
                receiverId: receiverId,
                msg: [
                    {senderName: 'Petya', content: 'Hello,Kostya'},
                    {senderName: 'Kostya', content: 'Hello,Petya'}
                ]
            }

            database.collection("user").insertOne(user, function (err, res) {
                if (err) throw err;
                console.log("1 document inserted");
                db.close();
            });

        });
    })

    socket.on('testUpdateSenderIdAndReceiverId', (user) => {
        console.log(user)
        // var jsonUser = JSON.parse(user)
        // console.log(jsonUser)
        // console.log(user['senderId'],user['receiverId'])

        var senderId = user['senderId']
        var receiverId = user['receiverId']
        var newMsg = user['messages']

        var jsonNewMsg = {
            msg: newMsg
        }

        MongoClient.connect(url, function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");

            var query = {
                senderId: senderId,
                receiverId: receiverId,
            }

            var newMessages = {$set: jsonNewMsg}

            database.collection("user").updateOne(query, newMessages, function (err, res) {
                if (err) throw err;
                console.log("1 document inserted");
                db.close();
            });

        });
    })

    socket.on('testReadSenderIdAndReceiverId', (user) => {
        var senderId = user['senderId']
        var receiverId = user['receiverId']

        MongoClient.connect(url, function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");

            var user = {
                senderId: senderId,
                receiverId: receiverId
            }

            database.collection("user").findOne(user, function (err, result) {
                if (err) throw err;
                console.log('msg', result['msg']);
                db.close();
            });

        });
    })


    socket.on('testSendMessage', (message) => {
        console.log(message)

        var senderId = message['senderId']
        var receiverId = message['receiverId']

        MongoClient.connect(url, function (err, db) {
            if (err) throw err;
            var database = db.db("chat_app_db");

            var user = {
                senderId: senderId,
                receiverId: receiverId
            }

            database.collection("user").findOne(user, function (err, result) {
                if (err) throw err;
                var jsonFoundMessages = result['msg']
                var listOfMessages = [];

                console.log('Print messages')

                // copy old messages to list
                jsonFoundMessages.forEach(function (message) {
                    var messageObject = Object();
                    messageObject.senderName = message.senderName;
                    messageObject.content = message.content;
                    listOfMessages.push(messageObject);
                });

                // add new message to list after coped old messages to there
                var newMessage = Object();
                newMessage.senderName = message['senderName'];
                newMessage.content = message['content'];

                listOfMessages.push(newMessage)

                // query for update messages
                var query = {
                    senderId: senderId,
                    receiverId: receiverId,
                }

                var jsonMessages = {
                    msg: listOfMessages
                }

                var newMessages = {$set: jsonMessages}

                database.collection("user").updateOne(query, newMessages, function (err, res) {
                    if (err) throw err;
                    console.log("1 document inserted");
                    db.close();
                });

            });

        });
    })


})



