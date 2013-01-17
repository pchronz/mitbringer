'use strict';

/* Controllers */


function OffersCtrl($scope, $rootScope, $location, Login, Offer, Message) {
  console.log("Active controller: Offers");
  $rootScope.activeView = 'listOffers';

  $scope.activePane = 'allOffers';
  $scope.activatePane = function(paneName) {
    $scope.activePane = paneName;

    // clear the queries
    $scope.originQuery = "";
    $scope.destinationQuery = "";
  }
  $scope.getPaneClass = function(paneName) {
    return $scope.activePane == paneName ? 'active' : '';
  }

  $scope.allOffers = Offer.getAllOffers();
  $scope.driverOffers = Offer.getDriverOffers();
  $scope.driverSearches = Offer.getDriverSearches();

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

  $scope.getOfferClass = function(offer) {
    return offer.isDriver ? 'success' : 'info';
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
  $scope.showNewOfferConfirmationModal = function(isDriver) {
    console.log('Displaying modal for new offer confirmation');
    $scope.isDriver = isDriver;
    if(Login.isLoggedIn()) $("#newOfferConfirmationModal").modal('show');
    else $location.path('/login');
  }

  $scope.createOffer = function(origin, destination, date, price, isDriver) {
    console.log("Creating a new offer: " + origin + " " + destination + " " + date + " " + price);
    $(".modal").modal("hide");
    Offer.createOffer(origin, destination, date, price, isDriver, isDriver);
  }

  $scope.closeModal = function() {
    console.log('Closing all modals');
    $(".modal").modal("hide");
  }

  $scope.queryAllOffers = function(originQuery, destinationQuery) {
    console.log("Querying all offers");
    $scope.allOffers = Offer.queryAllOffers(originQuery, destinationQuery);
  }
  $scope.queryDriverOffers = function(originQuery, destinationQuery) {
    console.log("Querying driver offers");
    $scope.driverOffers = Offer.querySpecialOffers(true, originQuery, destinationQuery);
  }
  $scope.queryDriverSearches = function(originQuery, destinationQuery) {
    console.log("Querying driver searches");
    $scope.driverSearches = Offer.querySpecialOffers(false, originQuery, destinationQuery);
  }
}
OffersCtrl.$inject = ['$scope', '$rootScope', '$location', 'Login', 'Offer', 'Message'];

function MessagesCtrl($scope, $rootScope, Offer, Message) {
  console.log("Active controller: Messages");
  $rootScope.activeView = 'listMessages';

  function getOffersSuccess() {
    $scope.receivedMessages = Message.getReceivedMessages();
    $scope.sentMessages = Message.getSentMessages();
  }
  $scope.offers = Offer.getAllOffers(getOffersSuccess);

  $scope.activePane = 'inbox';
  $scope.activatePane = function(pane) {
    $scope.activePane = pane;
  }
  $scope.getPaneClass = function(pane) {
    return $scope.activePane == pane ? 'active' : '';
  }

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

function LoginCtrl($scope, $rootScope, $location, Login) {
  console.log("Active controller: Login");
  $rootScope.activeView = 'login';

  $scope.loginName;
  $scope.loginPassword;

  $scope.authenticate = function(name, password) {
    function success() {
      console.log('Authentication successful');
      $location.path("/");
    }
    console.log('Authenticating as ' + name + "/" + password);
    Login.authenticate(name, password, success);
  }

  $scope.forgotPassword = false;
  $scope.showPasswordRecovery = function() {
    $("#passwordRecoveryModal").modal('show');
  }
  $scope.isResetting = false;
  $scope.resetSuccessful = false;
  $scope.resetPassword = function(username, email) {
    function success() {
      $scope.isResetting = false;
      $scope.resetSuccessful = true;
    }
    function failure() {
      $scope.isResetting = false;
      $scope.resetSuccessful = false;
    }
    console.log("Resetting username for " + username + "/" + email);
    $scope.isResetting = true;
    $scope.resetSuccessful = false;
    Login.resetPassword(username, email, success, failure);
  }
  $scope.closeModal = function() {
    console.log('Closing all modals');
    $(".modal").modal("hide");
  }
}
LoginCtrl.$inject = ['$scope', '$rootScope', '$location', 'Login'];

function RegisterCtrl($scope, $rootScope, $location, Login) {
  console.log("Active controller: Register");
  $rootScope.activeView = 'login';

  $scope.registerName;
  $scope.registerPassword;
  $scope.registerPassword2;
  $scope.registerEmail;

  $scope.isRegistering = false;
  $scope.registrationSuccess = false;

  $scope.register = function(username, password, password2, email) {
    function success() {
      $scope.isRegistering = false;
      $scope.registrationSuccess = true;
    }
    function failure() {
      $scope.isRegistering = false;
    }
    $scope.isRegistering = true;
    // validate the email address
    var emailValid = /[A-z|0-9]+@[A-z|0-9]+\.[A-z]+/.test(email);
    console.log("Email valid: " + emailValid);

    // validate the password
    var passwordSame = password == password2;
    console.log("Password same: " + passwordSame);
    var passwordValid = password.length >= 4;
    console.log("Password valid: " + passwordValid);

    // validate the username
    var usernameValid = username.length >= 4;
    console.log("Username valid: " + usernameValid);

    if(emailValid && passwordSame && passwordValid && usernameValid) Login.register(username, password, email, success, failure);
  }
}
RegisterCtrl.$inject = ['$scope', '$rootScope', '$location', 'Login'];

function ActivationCtrl($scope, $rootScope) {
  console.log("Active controller: Activation");
  $rootScope.activeView = 'login';
}
ActivationCtrl.$inject = ['$scope', '$rootScope'];

function ChangePasswordCtrl($scope, $rootScope, $location, Login) {
  console.log("Active controller: Change password");
  $rootScope.activeView = 'login';

  $scope.isNewPasswordValid = false;
  $scope.validateNewPasswords = function(currentPassword, newPassword1, newPassword2) {
    console.log("Checking new passwords");
    console.log(newPassword1 + " " + newPassword2);
    $scope.isNewPasswordValid = false;
    if(typeof newPassword1 === 'undefined') return;
    console.log("defined");
    if(newPassword1 != newPassword2) return;
    console.log("same");
    if(newPassword1.length < 4) return;
    console.log("length >= 4");
    if(currentPassword != Login.getAuthToken().password) return;
    console.log("old pass correct");
    $scope.isNewPasswordValid = true;
    console.log("New passwords are valid");
  }
  $scope.newPasswordSuccess = false;
  $scope.isChangingPassword = false;
  $scope.passwordChangeRequestDone = false;
  $scope.changePassword = function(newPassword) {
    function success() {
      $scope.newPasswordSuccess = true;
      $scope.isChangingPassword = false;
      $scope.passwordChangeRequestDone = true;
      Login.logout();
      $location.path('/login');
    }
    function failure() {
      $scope.newPasswordSuccess = false;
      $scope.isChangingPassword = false;
      $scope.passwordChangeRequestDone = true;
    }
    $scope.newPasswordSuccess = false;
    $scope.isChangingPassword = true;
    $scope.passwordChangeRequestDone = false;
    Login.changePassword(newPassword, success, failure);
  }
}
ChangePasswordCtrl.$inject = ['$scope', '$rootScope', '$location', 'Login'];

function NavCtrl($scope, $rootScope, $location, Login, Message) {
  console.log("Active controller: Nav");
  // if the user is not logged in, redirect to the login window
  $rootScope.$on("event:auth-loginRequired", function() {
    console.log("401 interceptor says hello");
    Login.logout();
    $location.path("/login");
  });

  $scope.getUsername = function() {
    return Login.getAuthToken().username;
  }

  $scope.numNewMessages = 0;
  function updateNumNewMessages() {
    function success(messages) {
      var m;
      for(m in messages) {
        var message = messages[m];
        if(message.state == 'unread') $scope.numNewMessages += 1;
      }
    }
    if(Login.isLoggedIn()) Message.getReceivedMessages(success);
  }
  updateNumNewMessages();
  
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
NavCtrl.$inject = ['$scope','$rootScope', '$location', 'Login', 'Message'];

