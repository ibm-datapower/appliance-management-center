/**
 * Copyright 2014 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
define(["dojo/on",
        "dojox/socket"],
		function(on,djxsocket){
	
	return function(socket, options){
		// summary:
		//		Provides auto-reconnection to a websocket after it has been closed
		// description:
		//		This module is a conversion of dojox.socket.Reconnect
		//		as an AMD module. The only change is that reconnect will always
		//		return a dojox/socket/LongPoll for reliability reasons.
		//	socket:
		//		Socket to add reconnection support to.
		// returns:
		// 		An object that implements the WebSocket API
		
		options = options || {};
		var reconnectTime = options.reconnectTime || 10000;
		
		var connectHandle = on(socket, "close", function(event){
			// Don't bother waiting for reconnection, it's clearly failed
			clearTimeout(checkForOpen);
			if(!event.wasClean){
				socket.disconnected(function(){
					djxsocket.replace(socket, newSocket = socket.reconnect());
				});
			}
		});
		
		var checkForOpen, newSocket;
		if(!socket.disconnected){
			// add a default impl if it doesn't exist
			socket.disconnected = function(reconnect){
				setTimeout(function(){
					reconnect();
					checkForOpen = setTimeout(function(){
						//reset the backoff
						if(newSocket.readyState < 2){
							reconnectTime = options.reconnectTime || 10000;
						}
					}, 10000);
				}, reconnectTime);
				// backoff each time
				reconnectTime *= options.backoffRate || 2;
			};
		}
		if(!socket.reconnect){
			// add a default impl if it doesn't exist
			socket.reconnect = function(){
				return djxsocket.LongPoll(socket.args);
			};
		}
		return socket;
	};
});
