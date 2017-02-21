(function() {
    var oldOpen = XMLHttpRequest.prototype.open;
    window.openHTTPs = 0;
    XMLHttpRequest.prototype.open = function(method, url, async, user, pass) {
      window.openHTTPs++;
      this.addEventListener("readystatechange", function() {
          if(this.readyState == 4) {
            window.openHTTPs--;
          }
        }, false);
      oldOpen.call(this, method, url, async, user, pass);
    }
})();