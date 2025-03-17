io_jmix_sampler_screen_ui_components_javascript_component_TimePicker = function () {
    let ranges = {};
    ranges["Today"] = [moment(), moment()];
    ranges["Tomorrow"] = [moment().subtract(-1, 'days'), moment().subtract(-1, 'days')];
    ranges["Yesterday"] = [moment().subtract(1, 'days'), moment().subtract(1, 'days')];
    ranges["Last 7 days"] = [moment().subtract(6, 'days'), moment()];
    ranges["This month"] = [moment().startOf('month'), moment().endOf('month')];
    ranges["Last month"] = [
        moment()
            .subtract(1, 'month')
            .startOf('month'),
        moment()
            .subtract(1, 'month')
            .endOf('month'),
    ];
    let moment_date_format = "YYYY-MM-DD"
    let dateRangeSettings = {
        ranges: ranges,
        startDate: moment(),
        endDate: moment(),
        locale: {
            cancelLabel: "Cancel",
            applyLabel: "Apply",
            customRangeLabel: "check",
            format: moment_date_format,
            toLabel: '~',
        },
    };
    $('#datetimePicker').daterangepicker(
        dateRangeSettings,
        function(start, end) {
            $('#datetimePicker').val(
                start.format(moment_date_format) + ' ~ ' + end.format(moment_date_format)
            );
        }
    );
};
