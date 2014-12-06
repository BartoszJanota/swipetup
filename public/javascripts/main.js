jQuery(document).ready(function ($) {
    var $timeline_block = $('.cd-timeline-block');

    //hide timeline blocks which are outside the viewport
    //animate timeline block which are inside the viewport
    $timeline_block.each(function () {
        if ($(this).offset().top > $(window).scrollTop() + $(window).height() * 0.75) {
            $(this).find('.cd-timeline-bubble, .cd-timeline-content').addClass('is-hidden');
        } else {
            $(this).find('.cd-timeline-bubble, .cd-timeline-content').addClass('bounce-in');
        }
    });

    //on scolling, show/animate timeline blocks when enter the viewport
    $(window).on('scroll', function () {
        $timeline_block.each(function () {
            if ($(this).offset().top <= $(window).scrollTop() + $(window).height() * 0.75 && $(this).find('.cd-timeline-bubble').hasClass('is-hidden')) {
                console.log('bounce in');
                $(this).find('.cd-timeline-bubble, .cd-timeline-content').removeClass('is-hidden').addClass('bounce-in');
            }
        });
    });
});