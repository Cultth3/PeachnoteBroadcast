jQuery(document).ready(function($) {

  var preview = {

    init: function() {
      this.switchByHash();
      this.addListeners();
    },

    addListeners: function() {
      var self = this;

      // Listen for changes to the #hash and update the breakpoint
      $(window).on('hashchange', function() {
        self.switchByHash();
      });

      // Add listeners to the buttons
      $('header .breakpoint #mobile-button').click(function(e) {
        e.preventDefault();
        self.switchTo('mobile');
      });

      $('header .breakpoint #desktop-button').click(function(e) {
        e.preventDefault();
        self.switchTo('desktop');
      });

      // Add the #desktop or #mobile anchor to the variant dropdown links
      $('.variant-dropdown .variant a').click(function(e) {
        e.preventDefault();
        window.location.href = $(this).attr('href') + window.location.hash;
      });
    },

    switchTo: function(breakpoint) {
      $('#page-preview-container').attr('class', breakpoint);
      $('header .breakpoint .button').removeClass('active');
      $('header .breakpoint .button#' + breakpoint + '-button').addClass('active');
      window.location.hash = '#' + breakpoint;

      if ( breakpoint === 'mobile' ) {
        // Attempt to adjust when the Iframe loads - in case it hasn't loaded yet
        $('#page-preview').on('load', this.adjustForScrollbars);

        // Attempt now as well, in case the Iframe is already loaded
        this.adjustForScrollbars();
      }
    },

    switchByHash: function() {
      if ( window.location.hash === '#mobile' ) {
        this.switchTo('mobile');
      } else {
        this.switchTo('desktop');
      }
    },

    adjustForScrollbars: function() {
      var iframe = $('iframe#page-preview'),
          iframeContent = iframe[0].contentWindow;

      // If we can't access the Iframe's DOM, give up
      if (  !iframeContent || !iframeContent.document ||
            iframeContent.document.documentElement.offsetWidth === 0 ) {
        return;
      }

      // Calculate how many vertical pixels are being removed from the Iframe's
      // viewport by the browser's scrollbar
      var scrollbarWidth = 320 - iframeContent.document.documentElement.offsetWidth;

      // If we don't need to adjust, don't do anything
      if ( scrollbarWidth < 1 ) { return; }

      // Compensate for scrollbars by making Iframe wider
      iframe.css('width', 320 + scrollbarWidth);
    }

  };

  preview.init();

});
