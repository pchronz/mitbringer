'use strict';

/* Services */

var serviceModule = angular.module('breakingBad.services', ["ngResource", "ngCookies"]);

serviceModule.factory("Login", loginFactory);
function loginFactory($resource) {
	var loginService = new Object();

	loginService.authToken = {username: "", password: ""};
	loginService.loginResource = $resource("http://localhost:port/logins", {port: ":9000"}, {
      authenticate: {method: "POST", isArray: false},
      register: {method: "PUT", isArray: false}
    });
	loginService.authenticate = function(username, password, success, failure) {
		console.log("Trying to authenticate...");
        function success() {
          console.log("Login successful");
          $.jStorage.set("authTokenUsername", username);
          $.jStorage.set("authTokenPassword", password);
        }
        function failure() {
          console.log("Login failed");
          $.jStorage.deleteKey("authTokenUsername");
          $.jStorage.deleteKey("authTokenPassword");
        }
		this.loginResource.authenticate({username: username, password: password}, {}, success, failure);
	};
	loginService.getAuthToken = function() {
		console.log("Getting auth token");
		var username = $.jStorage.get("authTokenUsername");
		var password = $.jStorage.get("authTokenPassword");
		if(!(username && password)) return {};
		return {username: username, password: password};
	};
	loginService.logout = function() {
		console.log("Logging out...");
		$.jStorage.deleteKey("authTokenUsername");
		$.jStorage.deleteKey("authTokenPassword");
	};
	loginService.isLoggedIn = function() {
		var username = $.jStorage.get("authTokenUsername");
		var password = $.jStorage.get("authTokenPassword");
        if(username == null || password == null) return false;
        else return username && password;
	};
	loginService.register = function(username, password, email, success, failure) {
      this.loginResource.register({username: username, password: password, email: email}, {}, success, failure);
    }

	return loginService;
};
loginFactory.$inject = ["$resource"];

serviceModule.factory("Offer", offerFactory);
function offerFactory($resource, Login) {
	var offerService = new Object();

	offerService.offerResource = $resource("http://localhost:9000:port/offers", {port: ":9000"}, {
      query: {method: "GET", isArray: true},
      create: {method: "PUT", isArray: false}
    });
	offerService.getOffers = function(originQuery, destinationQuery, dateQuery, success, failure) {
		console.log("Querying offers");
		return this.offerResource.query({},{},success, failure);
	};
	offerService.createOffer = function(origin, destination, date, price) {
      console.log('Creating new offer');
      var authToken = Login.getAuthToken();
      return this.offerResource.create({origin: origin, destination: destination, date: new Date(date).getTime(), price: price}, {username: authToken.username, password: authToken.password});
    }

	return offerService;
};
offerFactory.$inject = ["$resource", "Login"];

serviceModule.factory("Message", messageFactory);
function messageFactory($resource, Login) {
	var messageService = new Object();

	messageService.messageResource = $resource("http://localhost:9000:port/messages", {port: ":9000"}, {
      query: {method: "GET", isArray: true},
      send: {method: "PUT", isArray: false}
    });
	messageService.getMessages = function(success, failure) {
      console.log("Querying messages");
      var authToken = Login.getAuthToken();
      return this.messageResource.query({username: authToken.username, password: authToken.password}, success, failure);
	};
	messageService.sendMessage = function(content, offerId, destinationUser, success, failure) {
      console.log('Sending message');
      var originUser = $.jStorage.get("authTokenUsername");
      var authToken = Login.getAuthToken();
      this.messageResource.send({content: content, offerId: offerId, destinationUser: destinationUser, originUser: originUser}, {username: authToken.username, password: authToken.password}, success, failure);
    }

	return messageService;
};
messageFactory.$inject = ["$resource", "Login"];



