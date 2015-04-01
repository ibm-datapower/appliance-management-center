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
define({
	
	appliances:{
		url:"rest/appliances/",
		add:{
			method:"POST"
		},
		list:{
			method:"GET"
		}
	},
	
	appliance:{
		url:"rest/appliances/${applianceId}",
		list:{
			method:"GET"
		},
		update:{
			method:"PUT"
		},
		remove:{
			method:"DELETE"
		}
	},
	
	quiesceAppliance:{
		url:"rest/appliances/${applianceId}/actions/quiesce",
		add:{
			method:"POST"
		}
	},
	
	unquiesceAppliance:{
		url:"rest/appliances/${applianceId}/actions/unquiesce",
		add:{
			method:"POST"
		}
	},
	
	backupAppliance:{
		url:"rest/appliances/${applianceId}/actions/backup",
		add:{
			method:"POST"
		}
	},
	
	restoreAppliance:{
		url:"rest/appliances/${applianceId}/actions/restore",
		add:{
			method:"POST"
		}
	},
	
	rebootAppliance:{
		url:"rest/appliances/${applianceId}/actions/reboot",
		method:"POST"
	},
	
	applianceGroups:{
		url:"rest/appliances/${applianceId}/groups",
		update:{
			method:"PUT"
		}
	},
	
	domains:{
		url:"rest/domains/",
		list:{
			method:"GET"
		}
	},
	
	domainsWithName:{
		url:"rest/domains/${name}",
		list:{
			method:"GET"
		}
	},
	
	domainsWithNameConfig:{
		url:"rest/domains/${name}/config",
		deploy:{
			method:"POST"
		}
	},
	
	domain:{
		url:"rest/appliances/${applianceId}/domains/${name}",
		view:{
			method:"GET"
		},
		update:{
			method:"PUT"
		},
		remove:{
			method:"DELETE"
		}
	},
	
	domainConfig:{
		url:"rest/appliances/${applianceId}/domains/${name}/config",
		deploy:{
			method:"PUT"
		}
	},
	
	domainFiles:{
		url:"rest/appliances/${applianceId}/domains/${name}/files",
		upload:{
			method:"POST"
		}
	},
	
	domainGroups:{
		url:"rest/appliances/${applianceId}/domains/${name}/groups",
		update:{
			method:"PUT"
		}
	},
	
	quiesceDomain:{
		url:"rest/appliances/${applianceId}/domains/${name}/actions/quiesce",
		add:{
			method:"POST"
		}
	},
	
	unquiesceDomain:{
		url:"rest/appliances/${applianceId}/domains/${name}/actions/unquiesce",
		add:{
			method:"POST"
		}
	},
	
	restartDomain:{
		url:"rest/appliances/${applianceId}/domains/${name}/actions/restart",
		method:"POST"
	},
	
	services:{
		url:"rest/services/",
		list:{
			method:"GET"
		}
	},
	
	service:{
		url:"rest/appliances/${applianceId}/domains/${domainName}/services/${className}/${name}",
		add:{
			method:"PUT"
		},
		view:{
			method:"GET"
		},
		remove:{
			method:"DELETE" // To delete orphan objects as well, provide an array of orphan IDs in the body
		}
	},
	
	serviceOrphans:{
		url:"rest/appliances/${applianceId}/domains/${domainName}/services/${className}/${name}/orphans",
		list:{
			method:"GET"
		}
	},
	
	serviceImpact:{
		url:"rest/appliances/${applianceId}/domains/${domainName}/services/${className}/${name}/impact",
		list:{
			method:"GET"
		}
	},
	
	serviceContents:{
		url:"rest/services/contained",
		list:{
			method:"GET"
		}
	},
	
	quiesceService:{
		url:"rest/appliances/${applianceId}/domains/${domainName}/services/${className}/${name}/actions/quiesce",
		add:{
			method:"POST"
		}
	},
	
	unquiesceService:{
		url:"rest/appliances/${applianceId}/domains/${domainName}/services/${className}/${name}/actions/unquiesce",
		add:{
			method:"POST"
		}
	},
	
	actions:{
		url:"rest/actions/"
	},
	
	action:{
		url:"rest/actions/${id}"
	},
	
	currentUser:{
		url:"rest/current-user",
		list:{
			method:"GET"
		}
	},
	
	
	feedback:{
		url:"feedback",
		subscribe:{
			method:"GET"
		}
	},
	
	firmwares:{
		url:"rest/firmware/",
		list:{
			method:"GET"
		}
	},
	
	firmware:{
		url:"rest/firmware/${primaryKey}",
		get:{
			method:"GET"
		},
		update:{
			method:"PUT"
		},
		add:{
			url:"rest/firmware",
			method:"POST"
		}
	},
	
	availableFirmware:{
		url:"rest/appliances/${applianceId}/firmware/available"
	},
	
	currentFirmware:{
		url:"rest/appliances/${applianceId}/firmware/current"
	},
	
	permissions:{
		url:"rest/roles"
	},
	
	groups:{
		url:"rest/groups/",
		list:{
			method:"GET"
		}
	},
	
	upload:{
		url:"rest/files",
		method:"POST"
	},
	
	tempFiles:{
		url:"rest/files/${fileName}",
		download:{
			method:"GET",
		},
		remove:{
			method:"DELETE"
		}
	},
	
	help:{
		knowledgeCenter:{url:"http://pic.dhe.ibm.com/infocenter/wamcinfo/v5r0m0/topic/com.ibm.wamc.doc/common/welcome.html"},
		ibmSupport:{url:"http://www.ibm.com"}
	},
	
	pages:{
		management:"index.html",
		settings:"settings.html",
		login: "login",
		logout: "logout.jsp"
	},
	
	tabs:{
		appliances:"pages/appliances.html",
		domains: "pages/domains.html",
		services: "pages/services.html",
		repository: "pages/repository.html",
		history: "pages/history.html",
	}
	
});
