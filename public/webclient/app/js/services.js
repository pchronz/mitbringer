'use strict';

/* Services */

var serviceModule = angular.module('breakingBad.services', ["ngResource", "ngCookies"]);

serviceModule.factory("Login", loginFactory);
function loginFactory($resource) {
	var loginService = new Object();

	loginService.authToken = {username: "", password: ""};
	loginService.loginResource = $resource("http://www.die-mitbringer.de:port/logins", {port: ""}, {
		authenticate: {method: "POST", isArray: false}});
	loginService.authenticate = function(username, password, success, failure) {
		console.log("Trying to log in...");
		$.jStorage.set("authTokenUsername", username);
		$.jStorage.set("authTokenPassword", password);
		//return this.loginResource.authenticate({username: username, password: password}, {}, success, failure);
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

	return loginService;
};
loginFactory.$inject = ["$resource"];

serviceModule.factory("Offer", offerFactory);
function offerFactory($resource) {
	var offerService = new Object();

	offerService.getOffers = function(originQuery, destinationQuery, dateQuery) {
		console.log("Querying offers");
        var offers = [
          {
            id: 1,
            origin: 'Ikea Kassel',
            destination: 'Goettingen',
            date: new Date(),
            price: '5',
            user: 'fluppe97'
          },
          {
            id: 2,
            origin: 'Castrop',
            destination: 'Goettingen',
            date: new Date(),
            price: '5',
            user: 'mambojambo'
          }
        ]
		return offers;
	};

	offerService.createOffer = function(origin, destination, date, price) {
      console.log('Creating new offer');
    }

	return offerService;
};
offerFactory.$inject = ["$resource"];

serviceModule.factory("Message", messageFactory);
function messageFactory($resource) {
	var messageService = new Object();

	messageService.getMessages = function() {
		console.log("Querying messages");
        var messages = [
          {
            id: 1,
            originUser: 'bingolero',
            date: new Date(),
            offerId: 1,
            state: 'unread',
            content: 'Hi,\nKannst Du mir etwas mitbringen?\nBL'
          },
          {
            id: 2,
            originUser: 'roberta',
            date: new Date(),
            offerId: 2,
            state: 'read',
            content: 'Bring mir mal was mit'
          }
        ]
		return messages;
	};

	messageService.sendMessage = function(content, offerId, destinationUser) {
      console.log('Sending message');
    }

	return messageService;
};
messageFactory.$inject = ["$resource"];



