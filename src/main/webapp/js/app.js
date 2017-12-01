

var supportedServices = {
    'dropbox': false,
    'gdrive': false,
    'evernote': false
};

$.getJSON('/api/init', function (data) {
    console.log('init callback')
    var accessibleServices = data.accessibleServices;
    for (var service in supportedServices) {
        for (var i = 0; i < accessibleServices.length; i++) {
            if (accessibleServices[i] === service) {
                supportedServices[service] = true;
            } else {
            }
        }
    }

    init();
})



function init() {
    initServiceProviders();
    initUploading();
    initEventListeners();
}

function initEventListeners() {
    /**
     * Check if a provider check box is checked or unchecked.
     * We either add or remove the provider from the scope
     * and change visibility of 'grant access' button.
     */
    var providerCheckSelector = '#providers-list .provider-selection input[type="checkbox"]';
    $(providerCheckSelector).change(function () {
        var $providersList = $('#providers-list');
        var scopeList = $providersList.attr('data-scope-list');

        var $el = $(this);
        var service = $el.attr('data-service');

        if ($el.prop('checked')) {
            if (scopeList.indexOf(service) === -1) {
                scopeList = scopeList.trim() === ''
                        ? service
                        : scopeList + ' ' + service;
            }
        } else {
            if (scopeList.indexOf(service) !== -1) {
                scopeList = scopeList.replace(service, '').trim();
            }
        }

        $providersList.attr('data-scope-list', scopeList);
        $("#scope-input").val(scopeList);

        var unauthorizedSelected = false;

        $(providerCheckSelector).each(function () {
            var $el = $(this);
            var service = $el.attr('data-service');
            if ($el.prop('checked') && !supportedServices[service]) {
                unauthorizedSelected = true;
            }
        });

        if (unauthorizedSelected) {
            $('#authorize-btn').css('visibility', 'visible');
        } else {
            $('#authorize-btn').css('visibility', 'hidden');
        }
     });

    /**
     * On button click, the user will be redirected to
     * the Oauth authorization page
     */
    $('#authorize-btn').click(function () {
        var scopeList = $('#providers-list').attr('data-scope-list');
        if (scopeList.trim() !== '') {
            $.post('/api/oauth2/flow', { scope: scopeList }, function (data, status, jqHxr) {
                if (jqHxr.status === 201) {
                    var location = jqHxr.getResponseHeader('location');
                    window.location.replace(location);
                } else if (jqXhr.status === 204) {
                    console.log('No Content');
                } else {
                    console.log('Unexpected result: ', data)
                }
            })
        }
    })
}

/**
 * Populate the service providers list
 */
function initServiceProviders() {
    var $providersList = $('#providers-list');

    var scopeList = '';

    for (var service in supportedServices) {
        var $provider = $('#templates .provider-container').clone();

        $provider.find('.provider-logo img')
            .attr('src', '/img/' + service + '.png')
            .attr('alt', service + ' logo');

        $provider.find('.provider-status')
            .text(function () {
                return supportedServices[service]
                    ? 'Authorized'
                    : ''
            });

        $provider.find('.provider-selection input[type="checkbox"]')
            .attr('data-service', service)
            .prop('checked', function () {
                return supportedServices[service]
            });

        $providersList.append($provider);

        if (supportedServices[service]) {
            scopeList = scopeList === ''
                    ? service
                    : scopeList + ' ' + service;
        }

        $providersList.attr('data-scope-list', scopeList);
        $('#scope-input').val(scopeList);
    }
}

function initUploading() {
    var fileList = new Array();

    $('#upload-file').fileupload({
        add: function (e, data) {
            console.log(data)
            var file = data.files[0]
            var filename = file.name;
            console.log('filename before: ', filename)
            if (filename.length > 32) {
                filename = filename.substring(0, 32) + "..."
            }
            console.log('filename after: ', filename)
            $('.custom-file-input').next('.form-control-file').addClass("selected").html(filename);
            $('.submit-btn').prop('disabled', false);
            fileList[0] = file;
        },
        progressall: function (e, data) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            $('.progress .bar').css(
                'width',
                progress + '%'
            );
        },
        progressServerRate: 0.3,
        progressServerDecayExp: 2
    });

    $('#upload-file').bind('fileuploadsubmit', function (e, data) {
        // The example input, doesn't have to be part of the upload form:
        console.log(data);
//        var input = $('#input');
//        data.formData = { : input.val()};
//        if (!data.formData.example) {
//            data.context.find('button').prop('disabled', false);
//            input.focus();
//            return false;
//        }
    });

    $('.submit-btn').click(function (e) {
        e.preventDefault();

        if (fileList.length > 0) {
            $('.progress').css('visibility', 'visible');
            $('.submit-btn').prop('disabled', true);

            $('#upload-file').fileupload('send', { files: fileList })
                .success(function (result, textStatus, jqXHR) {
                    console.log('success: ' + JSON.stringify(result))
                })
                .error(function (jqXHR, testStatus, errorThrown) {
                    console.log('error: ' + errorThrown);
                })
                .complete(function (result, textStatus, jqXHR) {
                    console.log('complete: ' + JSON.stringify(result));
                    $('.progress').css('visibility', 'hidden');
                    $('.submit-btn').prop('disabled', false);
                    $('.progress .bar').css('width', '0%');
                });

        }
    });
}
