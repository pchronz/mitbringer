'use strict';

/* Services */

var serviceModule = angular.module('breakingBad.services', ["ngResource", "ngCookies"]);

serviceModule.factory("Login", loginFactory);
function loginFactory($resource) {
	var loginService = new Object();

	loginService.authToken = {username: "", password: ""};
	loginService.loginResource = $resource("http://localhost:port/logins/:reset", {port: ":9000"}, {
      authenticate: {method: "POST", isArray: false},
      resetPassword: {method: "POST", isArray: false},
      changePassword: {method: "POST", isArray: false},
      register: {method: "PUT", isArray: false}
    });
	loginService.authenticate = function(username, password, success, failure) {
		console.log("Trying to authenticate...");
        function successLocal() {
          console.log("Login successful");
          $.jStorage.set("authTokenUsername", username);
          $.jStorage.set("authTokenPassword", password);
          success();
        }
        function failureLocal() {
          console.log("Login failed");
          $.jStorage.deleteKey("authTokenUsername");
          $.jStorage.deleteKey("authTokenPassword");
          failure();
        }
		this.loginResource.authenticate({username: username, password: password}, {}, successLocal, failureLocal);
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
	loginService.resetPassword = function(username, email, success, failure) {
      this.loginResource.resetPassword({username: username, email: email, reset: 'reset'}, {}, success, failure);
    }
	loginService.changePassword = function(newPassword, success, failure) {
      var authToken = this.getAuthToken();
      this.loginResource.changePassword({newPassword: newPassword, reset: 'password'}, {username: authToken.username, password: authToken.password}, success, failure);
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
	offerService.getAllOffers = function(success, failure) {
		console.log("Querying offers");
		return this.offerResource.query({},{},success, failure);
	};
	offerService.queryAllOffers = function(origin, destination, success, failure) {
		console.log("Querying all offers for " + origin + "/" + destination);
        if(typeof origin == 'undefined')
          return this.offerResource.query({destination: destination},{},success, failure);
        if(typeof destination == 'undefined')
          return this.offerResource.query({origin: origin},{},success, failure);
        return this.offerResource.query({origin: origin, destination: destination},{},success, failure);
	};
	offerService.getDriverOffers = function(success, failure) {
		console.log("Querying driver offers");
		return this.offerResource.query({isDriver: 1}, {}, success, failure);
	};
	offerService.getDriverSearches = function(success, failure) {
		console.log("Querying driver searches");
		return this.offerResource.query({isDriver: 0}, {}, success, failure);
	};
	offerService.querySpecialOffers = function(isDriver, origin, destination, success, failure) {
		console.log("Querying special offers for " + origin + "/" + destination);
        var isDrivr = isDriver ? 1 : 0;
        if(typeof origin == 'undefined')
          return this.offerResource.query({isDriver: isDrivr, destination: destination},{},success, failure);
        if(typeof destination == 'undefined')
          return this.offerResource.query({isDriver: isDrivr, origin: origin},{},success, failure);
        return this.offerResource.query({isDriver: isDrivr, origin: origin, destination: destination},{},success, failure);
	};
	offerService.createOffer = function(origin, destination, date, price, isDriver, success, failure) {
      console.log('Creating new offer');
      var isDriverNum = isDriver ? 1 : 0;
      var authToken = Login.getAuthToken();
      return this.offerResource.create({origin: origin, destination: destination, date: new Date(date).getTime(), price: price, isDriver: isDriverNum}, {username: authToken.username, password: authToken.password}, success, failure);
    }

	return offerService;
};
offerFactory.$inject = ["$resource", "Login"];

serviceModule.factory("Message", messageFactory);
function messageFactory($resource, Login) {
	var messageService = new Object();

	messageService.messageResource = $resource("http://localhost:port/messages/:box", {port: ":9000"}, {
      query: {method: "GET", isArray: true},
      send: {method: "PUT", isArray: false}
    });
	messageService.getReceivedMessages = function(success, failure) {
      console.log("Querying received messages");
      var authToken = Login.getAuthToken();
      return this.messageResource.query({box: 'received', username: authToken.username, password: authToken.password}, {}, success, failure);
	};
	messageService.getSentMessages = function(success, failure) {
      console.log("Querying sent messages");
      var authToken = Login.getAuthToken();
      return this.messageResource.query({box: 'sent', username: authToken.username, password: authToken.password}, success, failure);
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

serviceModule.factory("Notification", notificationFactory);
function notificationFactory($rootScope) {
  var notificationService = new Object();
  notificationService.errors = [];
  notificationService.pullNotifications = function() {
    console.log("Pulling notifications");
    var ms = this.notifications;
    this.notifications = [];
    return ms;
  };
  notificationService.pullErrors = function() {
    console.log("Pulling errors");
    var ms = this.errors;
    this.errors = [];
    return ms;
  };
  notificationService.pushNotification = function(notification, silent) {
    console.log("Pushing notification");
    this.notifications.push(notification);
    if(!silent)
      $rootScope.$broadcast('event:new-notification');
  };
  notificationService.pushError = function(error, silent) {
    console.log("Pushing error");
    this.errors.push(error);
    if(!silent)
      $rootScope.$broadcast('event:new-notification');
  };
  return notificationService;
};
notificationFactory.$inject = ["$rootScope"];


