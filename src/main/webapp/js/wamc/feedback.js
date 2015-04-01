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
define(["dojo/_base/array",
        "dojo/_base/declare",
        "dojo/_base/json",
        "dojo/_base/lang",
        "dojo/_base/unload",
        "dojo/topic",
        "dojox/socket",
        "wamc/socket/Reconnect"],
        function(array,declare,json,lang,baseUnload,topic,socket,Reconnect){

	var feedback = {
		
		_socket:null,
		
		start:function(/*String*/url,/*String*/topic){
			// summary:
			//		Creates a reconnecting dojox socket that polls a url and publishes
			//		the resulting event data to the topic specified by topic
			//		In the event that the socket returns an error, this socket will
			//		cause the page to reload.
			//		The socket will attempt to close on page unload
			
			// Create the socket
			var t=this,
				s = socket.LongPoll({
				url:url,
				headers: {
					"Accept": "application/json",
					"Content-Type": "application/json"
				}
			});
	
			s = Reconnect(s);
			
			// When a message is received, publish the event
			s.on("message",function(event){
				event.topic=topic;
				t.publishEvent(event);
			});
			
			
			s.on("error",function(event){
				if (event.error && event.error.status == 401) window.location.reload(true);
			});
			
			baseUnload.addOnWindowUnload(function(){
				t.stop();
			});
			
			t._socket = s;
			
			return s;
		},
		
		publishEvent:function(event){
			var F="wamc.feedback.publishEvent()",data;
			try{
				//Convert json event data into an array of objects
				data = json.fromJson(event.data);
				
				// Publish each event on the specified topic
				array.forEach(data,function(item){
					console.debug(F,"publishing",item);
					topic.publish(event.topic,item);
				});
			}
			catch(error) {
				console.debug(F,"Error",error);
				// What came back wasn't valid JSON.
				if(event.data.indexOf("wamc-redirect-to-login") > -1){
					// This was the login page. Nothing more of any use will be
					// available from the server, so refresh the UI causing a 
					// bounce to the login page.
					window.location.reload(true);
				}
				// Otherwise, just ignore the invalid response.
			}
		},
		
		stop: function(){
			// summary:
			//		stop polling

			var F = "wamc.feedback.stop()";
			console.debug(F);
			
			this._socket && this._socket.close();
		}
		
		
	};

	return feedback;
});