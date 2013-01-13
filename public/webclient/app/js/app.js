'use strict';


// Declare app level module which depends on filters, and services
angular.module('breakingBad', ['breakingBad.filters', 'breakingBad.services', 'breakingBad.directives']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/', {templateUrl: 'partials/listOffers.html', controller: OffersCtrl});
    $routeProvider.when('/listOffers', {templateUrl: 'partials/listOffers.html', controller: OffersCtrl});
    $routeProvider.when('/listMessages', {templateUrl: 'partials/listMessages.html', controller: MessagesCtrl});
    $routeProvider.when('/login', {templateUrl: 'partials/login.html', controller: LoginCtrl});
    $routeProvider.when('/register', {templateUrl: 'partials/register.html', controller: RegisterCtrl});
    $routeProvider.otherwise({redirectTo: '/'});
  }]);
