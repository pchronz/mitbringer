'use strict';

/* Controllers */


function OffersCtrl($scope, $rootScope, $location, Login, Offer, Message) {
  $rootScope.activeView = 'listOffers';

  $scope.offers = Offer.getOffers();

  $scope.dateToString = function(d) {
    var date = new Date(parseInt(d));
    if(date) {
      var d = date.getDate();
      var month = date.getMonth() + 1;
      var year = date.getFullYear();
      return d.toString() + "." + month + "." + year;
    }
    else return "";
  }

  $scope.activeOffer;

  // contact re offer
  $scope.contactOffer = function(offer) {
    console.log('Showing offer contact for offer: ' + offer.id);
    $scope.activeOffer = offer;
    if(Login.isLoggedIn()) $("#offerContactModal").modal('show');
    else $location.path('/login');
  }
  $scope.sendMessage = function(message, offer) {
    console.log('Sending message ' + message + ' for offer ' + offer.id);
    $(".modal").modal("hide");
    Message.sendMessage(message, offer.id, offer.user);
  }

  // confirm offer creation
  $scope.showNewOfferConfirmationModal = function() {
    console.log('Displaying modal for new offer confirmation');
    if(Login.isLoggedIn()) $("#newOfferConfirmationModal").modal('show');
    else $location.path('/login');
  }

  $scope.createOffer = function(origin, destination, date, price) {
    console.log("Creating a new offer: " + origin + " " + destination + " " + date + " " + price);
    $(".modal").modal("hide");
    Offer.createOffer(origin, destination, date, price);
  }

  $scope.closeModal = function() {
    console.log('Closing all modals');
    $(".modal").modal("hide");
  }
}
OffersCtrl.$inject = ['$scope', '$rootScope', '$location', 'Login', 'Offer', 'Message'];

function MessagesCtrl($scope, $rootScope, Offer, Message) {
  $rootScope.activeView = 'listMessages';

  $scope.offers = Offer.getOffers();
  $scope.messages = Message.getMessages();

  // helper function for displaying messages
  $scope.getOfferForMessage = function(message, field) {
    if(!message) return null;
    var offer;
    var o;
    for(o in $scope.offers) {
      var offer_t = $scope.offers[o]
      if(offer_t.id == message.offerId) offer = offer_t;
    }
    if(offer) {
      if(field) return offer[field];
      else return offer;
    }
    else return null;
  }
  $scope.dateToString = function(d) {
    var date = new Date(parseInt(d));
    if(date) {
      var d = date.getDate();
      var month = date.getMonth() + 1;
      var year = date.getFullYear();
      return d.toString() + "." + month + "." + year;
    }
    else return "";
  }
  $scope.getMessageClass = function(message) {
    return message.state == 'unread' ? 'info' : '';
  }

  $scope.viewMessage = function(message) {
    $scope.activeMessage = message;
    message.state = 'read';
    $("#viewMessageModal").modal('show');
  }

  $scope.closeModal = function() {
    console.log('Closing all modals');
    $(".modal").modal("hide");
    return false;
  }

  $scope.showResponseModal = function(message) {
    // first hide all previous modals
    $scope.closeModal();
    // select the provided message
    $scope.activeMessage = message;
    // prepare the response to include the previous conversation
    $scope.responseContent = $scope.prepareResponseMessage(message.content);
    // now open the response modal
    $("#responseModal").modal('show');
  }

  $scope.prepareResponseMessage = function(content) {
    // add indentation at each new line
    var response = content;
    var respSplit = response.split('\n');
    // add two new lines at the beginning
    response = "\n\n";
    var l_it;
    for(l_it in respSplit) {
      var line = respSplit[l_it];
      line = "<< " + line + "\n";
      response = response + line;
    }
    return response;
  }

  $scope.sendResponse = function(message, content) {
    console.log('Sending response to message.id ' + message.id + ' with content \n' + content);
    $scope.closeModal();
    Message.sendMessage(content, message.offerId, message.originUser);
  }
}
MessagesCtrl.$inject = ['$scope', '$rootScope', 'Offer', 'Message'];

function LoginCtrl($scope, $rootScope, Login) {
  $rootScope.activeView = 'login';

  $scope.loginName;
  $scope.loginPassword;

  $scope.authenticate = function(name, password) {
    console.log('Authenticating as ' + name + "/" + password);
    Login.authenticate(name, password);
  }
}
LoginCtrl.$inject = ['$scope', '$rootScope', 'Login'];

function RegisterCtrl($scope, $rootScope, $location, Login) {
  $rootScope.activeView = 'login';

  $scope.registerName;
  $scope.registerPassword;
  $scope.registerPassword2;
  $scope.registerEmail;

  $scope.register = function(name, password, password2, email) {
    function success() {
      $location.path('/login');
    }
    function failure() {
    }
    if(password == password2) Login.register(name, password, email, success, failure);
  }
}
RegisterCtrl.$inject = ['$scope', '$rootScope', '$location', 'Login'];

function NavCtrl($scope, $rootScope, $location, Login) {
  // if the user is not logged in, redirect to the login window
  $rootScope.$on("event:auth-loginRequired", function() {
    console.log("401 interceptor says hello");
    Login.logout();
    $location.path("/login");
  });
  
  $scope.views = [
    {
      name: 'listOffers',
      label: 'Angebote'
    },
    {
      name: 'listMessages',
      label: 'Nachrichten'
    }
  ];
  $scope.getViewClass = function(view) {
    return $rootScope.activeView == view ? 'active' : '';
  };
  $scope.getShow = function(view) {
    if(view.name == 'listMessages') return Login.isLoggedIn();
    return true;
  }

  $scope.logout = function() {
    Login.logout();
  }

  $scope.isLoggedIn = function() {
    return Login.isLoggedIn();
  }
}
NavCtrl.$inject = ['$scope','$rootScope', '$location', 'Login'];

