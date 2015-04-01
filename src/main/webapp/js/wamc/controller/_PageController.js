/** * Copyright 2014 IBM Corp. * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. **/
define(["dojo/_base/array",
        "dojo/_base/declare",
        "dojo/_base/lang",
        "dojo/_base/window",
        "dojo/_base/xhr",
        "dojo/aspect",
        "dojo/Deferred",
        "dojo/dom-style",        "dojo/dom-construct",
        "dojo/hash",
        "dojo/io-query",
        "dojo/string",
        "dojo/topic",
        "dojo/when",
        "dijit/layout/BorderContainer",
        "dojox/layout/ContentPane",
        "dijit/layout/TabContainer",
        "dijit/Menu",
        "dijit/MenuItem",
        "dijit/MenuBar",
        "dijit/MenuBarItem",        "dijit/PopupMenuBarItem",
        "dijit/registry",
        "dijit/Dialog",        "wamc/config/Resources",
        "wamc/feedback",
        "wamc/widget/messaging/FeedbackBar",
        "wamc/user",
        "dojo/text!wamc/controller/templates/aboutBox.html",
        "dojo/i18n!wamc/nls/strings"],
		function(array,declare,lang,win,xhr,aspect,Deferred,domStyle,domConstruct,hash,
				ioQuery,string,topic,when,BorderContainer,ContentPane,
				TabContainer,Menu,MenuItem,MenuBar,MenuBarItem,PopupMenuBarItem,registry,
				Dialog,Resources,feedback,
				FeedbackBar,user,aboutBoxTemplate,nls){
	
	// Replace dojo _isDocumentOk to only allow 0 response codes for titanium/chromium/localfiles
	aspect.around(dojo,"_isDocumentOk", function(originalFunc){
		return function(http){
			var stat = http.status || 0,
				lp = location.protocol;
			stat =
				(stat >= 200 && stat < 300) || // allow any 2XX response code
				stat == 304 ||                 // or, get it out of the cache
				stat == 1223 ||                // or, Internet Explorer mangled the status code
				(!stat && (lp == "file:" || lp == "chrome:" || lp == "chrome-extension:" || lp == "app:"));// or, we're Titanium/browser chrome/chrome extension requesting a local file
			return stat; // Boolean
		};
	});
	
	// Reload to force login if XHR request fails with 401
	aspect.before(dojo, "_ioSetArgs", function(args, canceller, okHandler, errHandler) {
		var origHandler = args.error;
		args.error = function(error) {
			if (error.xhr && error.xhr.status == 401) window.location.reload(true);
			else if (origHandler) origHandler(error);
		};
		return [args, canceller, okHandler, errHandler];
	 });

	var controllerHash = {
			"appliances":"wamc/controller/appliances",
			"domains":"wamc/controller/domains",
			"services":"wamc/controller/services",
			"repository":"wamc/controller/repository",
			"history":"wamc/controller/history"
	},
	
	_URLFromQueryObject = function(/*String*/uri,/*Object*/queryObj){
		// summary:
		//		Build a url from a uri and query object
		// uri: String
		//		The first part of the url e.g. /amc/domains.html
		// queryObj: object
		//		The object representing the query string
		// return: String
		//		The completed URL
		var F = "_URLFromQueryObject():";
		console.debug(F,"Entering",uri,queryObj);
					
		var url = uri;
		
		if(queryObj && queryObj !== {}){
			url = url + "?" + ioQuery.objectToQuery(queryObj);
		}
		
		console.log(F,"Exiting",url);
		return url;
	},
	
	redirect = function(/*String*/uri,/*Object*/params){
		// summary:
		//		Redirect the browser to the specified uri
		// uri: String
		//		The URI to redirect to
		// params: Object
		//		The parameters to use in the query string
		
		var url = _URLFromQueryObject(uri, params);
		
		window.location = url;
	},
	
	PageController = declare(null,{
		// summary:
		//		Base object for page controllers. Not intended to be used directly.
		
		name:"",
		
		tabs: [],
		
		actions:[{name:"management"}],
		
		_buildUserMenu:function(){
			
			var userMenu = new Menu({"aria-label":nls.userMenu.label});
			
			userMenu.addChild(new MenuItem({
				label: nls.userMenu.logOut,
				onClick: user.logout
			}));
			
			return userMenu;
		},
		
		_buildHelpMenu:function(){
			
			var helpMenu = new Menu({"aria-label":nls.helpMenu.label});
			
			helpMenu.addChild(new MenuItem({
				label: nls.helpMenu.help,
				onClick: function(){
						window.open(Resources.help.knowledgeCenter.url);}
			}));
			helpMenu.addChild(new MenuItem({
				label: nls.helpMenu.about,
				onClick: function(){
					new Dialog({
						title: nls.aboutBox.title,
						content: string.substitute(aboutBoxTemplate, nls.aboutBox),						style: "width:500px;",
						closeable: true
					}).show();
				}
			}));
			
			return helpMenu;
		},
		
		_buildHeader:function() {			var header = new MenuBar({				region:"top",				style:"width:100%;"			}, "header"),			userMenu = this._buildUserMenu(),			helpMenu = this._buildHelpMenu(),			userMenuPopup = new PopupMenuBarItem({				label: user.getUserName(),				popup: userMenu,				style: "float:right;"			}, "userMenuPopup"),			helpMenuPopup = new PopupMenuBarItem({				label: "?",				popup: helpMenu,				style: "float:right;"			}, "helpMenuPopup");			header.addChild(new ContentPane({innerHTML: "<b><font size=\"+1\">IBM Appliance Management Center</font></b>", style: "float:left;"}));			header.addChild(helpMenuPopup);			header.addChild(userMenuPopup);			header.startup();			return header;		},
		
		filterContent:function(/*String*/hashId,/*Object*/filter){
			// summary:
			//		Swap to a tab and filter the grid with specified criteria
			// hashId: String
			//		The hashId of the tab being switched to
			// filter: Object
			//		The filter criteria to use
			
			var contentStack = registry.byId("contentStack"),
				controllerMid = controllerHash[hashId],
				st = this.selectTab;
				//switched = this.selectTab(hashId);
			
			if(!controllerMid || !filter) return;
			
			require([controllerMid],function(controller){
				if(typeof controller.setFilter === "function"){
					controller.setFilter(filter);
					st(hashId);
				}
			});
			
		},
		
		selectTab:function(/*String*/hashId){
			// summary:
			//		Select the tab with the specified hashId the main content stack
			// hashId: String
			//		The hash Id of the tab to select
			// return: boolean
			//		True if the tab is selected, otherwise false
			
			var stackContainer = registry.byId("contentStack");
			
			if(stackContainer.selectedChildWidget.hashId === hashId){
				// If the tab is already selected, do nothing
				return true;
			}
			
			return array.some(stackContainer.getChildren(),function(child){
				
				if(child.hashId && child.hashId === hashId){
					stackContainer.selectChild(child);
					return true;
				}
				return false;
			});
			
		},
		
		refreshChildContent:function(/*String*/hashId){
			var controllerMid = controllerHash[hashId];
			
			if(!controllerMid) return;
			
			require([controllerMid],function(controller){
				if(typeof controller.refreshContent === "function"){
					controller.refreshContent();
				}
			});
			
		},
		
		checkPermission:function(){
			// summary:
			//		Make sure the currently logged in user has permission 
			//		to access this page
			
			// get the permission for this page
			var n = this.name,
				permissionMap = {},
				firstPage = null;
				

			// Work out which pages the user has permissions for
			array.forEach(this.actions,function(act){
				var hasPermission = false;
				
				hasPermission = !act.permission || 
					array.some(act.permission,function(permission){
						return user.hasPermission(permission);
					});
				
				if(hasPermission && !firstPage){
					firstPage = act.name;
				}
				
				permissionMap[act.name] = hasPermission;
			},this);
			

			if(permissionMap[this.name]===true) {
				// If the user has permission for this page, continue
				return;
			}else if (!!firstPage){
				// Otherwise, redirect a page they have permission for
				redirect(Resources.pages[firstPage]);
			}else{
				user.logout();	
			}
			
		},
		
		initUI:function(){
			
			var	template = new BorderContainer({
						gutters:false,
						style:"width:100%;height:100%;"
					},"template"),
				header = this._buildHeader(),
				main = new BorderContainer({region:"center"},"main"),
				feedbackBar = new FeedbackBar({region:"top",
					"aria-label":nls.regions.general.feedbackBar,
					role:"complementary",
					style:"height:0px"},
				"feedbackBar"),
			
				contentStack = new TabContainer({region:"center","class":"wamcContentStack"},"contentStack"),
				createContentTab;
						template.addChild(header);
			
			template.addChild(main);
			
			main.addChild(feedbackBar);
			
			main.addChild(contentStack);
			
			createContentTab = function(/*String*/name,container){
				var selectTab = (hash() === name), 
					tab = new ContentPane({title:nls.global[name],
						parseOnLoad:false,
						href:Resources.tabs[name],
						selected:selectTab,
						executeScripts:true,
						hashId:name,
						"aria-label":nls.global[name],
						"class":"wamcContentPage"});

				container.addChild(tab);
			};

			array.forEach(this.tabs,function(tab){
				var permissionRequired = typeof tab.permission === "string";
				if(!permissionRequired || user.hasPermission(tab.permission)){
					createContentTab(tab.name,contentStack);
				}
			});
			
//			header.set("contentContainer",contentStack);
			
			template.startup();
			
			hash(contentStack.selectedChildWidget.hashId);
		},
		
		setupEvents:function(){
			
			var t = this,
				contentStack = registry.byId("contentStack");
			
			topic.subscribe(contentStack.id + "-selectChild", function(child){
				hash(child.hashId);
				t.refreshChildContent(child.hashId);
			});
			
			topic.subscribe("/dojo/hashchange",this.selectTab);
			
			when(contentStack.selectedChildWidget.onLoadDeferred,function(){
				feedback.start(Resources.feedback.url,"actionStatus");
			});
			
			topic.subscribe("setFilter",lang.hitch(this,this.filterContent));
			
		},
		
		init:function(){
			this.checkPermission();
			this.initUI();
			this.setupEvents();

		}
	});
	
	return PageController;
});
