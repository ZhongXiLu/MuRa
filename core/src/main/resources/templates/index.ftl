<!DOCTYPE html>
<html lang="en" style="height: 100%">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.19.0/themes/prism.min.css"
          integrity="sha256-cuvic28gVvjQIo3Q4hnRpQSNB0aMw3C+kjkR0i+hrWg=" crossorigin="anonymous"/>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.19.0/plugins/line-numbers/prism-line-numbers.min.css"
          integrity="sha256-Afz2ZJtXw+OuaPX10lZHY7fN1+FuTE/KdCs+j7WZTGc=" crossorigin="anonymous"/>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.19.0/plugins/line-highlight/prism-line-highlight.min.css"
          integrity="sha256-FFGTaA49ZxFi2oUiWjxtTBqoda+t1Uw8GffYkdt9aco=" crossorigin="anonymous"/>
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/bs4/dt-1.10.20/datatables.min.css"/>
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/10.6.2/css/bootstrap-slider.min.css"/>
    <style type="text/css">
        .tooltip.in {
            opacity: 1;
        }
        td:nth-child(1) {
            font-weight: bold;
        }
    </style>

    <title>MuRa</title>
</head>

<body style="height: 100%">
<div class="container-fluid h-100">
    <div class="row h-100">
        <div class="col-2 bg-dark text-light">
            <div class="sticky-top">
                <#-- Input of the coefficients weights -->
                <br><br>
                <h2>Customize Parameters</h2>
                <div id="rankers">
                    <#list rankers as rank>
                    <br><br><h5>${rank.getRanker()}</h5>
                    <input
                            style="width: 100%;"
                            id="${rank.getRanker()}" data-slider-id='${rank.getRanker()}' type="text" data-slider-min="0" data-slider-max="1" data-slider-step="0.001" data-slider-value="0.5"
                    />
                    </#list>
                </div>
            </div>
        </div>

        <div class="col-10" id="main">
            <#-- Table with mutants -->
            <br><br>
            <h1>Ranked Mutants</h1>
            <br>
            <table id="mutants" class="table table-striped table-bordered table-hover">
                <thead class="thead-dark">
                <tr>
                    <th>Score</th>
                    <#list rankers as rank>
                    <th>${rank.getRanker()}</th>
                    </#list>
                    <th>Mutated Method</th>
                    <th>Mutator</th>
                </tr>
                </thead>
                <tbody>
                <#list mutants as mutant>
                    <tr data-toggle="modal" data-target="#mutantModal"
                        data-lineNr="${mutant.getLineNr()}" data-mutatedClass="${mutant.getMutatedClass()}"
                        data-filepath="${mutant.getOriginalFile().getCanonicalPath()}"
                        data-filename="${mutant.getOriginalFile().getName()}"
                        data-mutator="<ul><li><b>${mutant.mutator}</b>: ${mutant.notes}</li></ul>"
                        data-mutatedMethod="<#if mutant.mutatedMethod == '<init>'><samp>${mutant.mutatedClass}.<b>&lt;init&gt;</b><i>${mutant.mutatedMethodDescr}</i></samp><#else><samp>${mutant.mutatedClass}.<b>${mutant.mutatedMethod}</b><i>${mutant.mutatedMethodDescr}</i></samp></#if>"
                        data-coeffs="<ul><#list mutant.getRankCoefficients() as rank><li><b>${rank.getRanker()}</b>: ${rank.getValue()} (${rank.getExplanation()})</li></#list></ul>"
                        data-location="${mutant.getLines(5)}" <#if mutant.survived()> class="table-success mutant" <#else> class="table-danger mutant" </#if>>
                        <td class="score" data-score="${mutant.getRawScore()}">${mutant.getRawScore()}</td>

                        <#list mutant.getRankCoefficients() as rank>
                            <td class="${rank.getRanker()}" data-toggle="tooltip" data-delay="200" data-placement="top"
                                title="${rank.getExplanation()}">${rank.getValue()}</td>
                        </#list>

                        <#if mutant.mutatedMethod == "<init>">
                        <td><samp>${mutant.mutatedClass}.<b>&lt;init&gt;</b><i>${mutant.mutatedMethodDescr}</i></samp></td>
                        <#else>
                        <td><samp>${mutant.mutatedClass}.<b>${mutant.mutatedMethod}</b><i>${mutant.mutatedMethodDescr}</i></samp></td>
                        </#if>
                        <td data-toggle="tooltip" data-delay="200" data-placement="top"
                            title="${mutant.notes}">${mutant.mutator}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
            <br><br>
        </div>
    </div>

    <#-- Modal to show additional information about the mutant -->
    <div class="modal fade" id="mutantModal" tabindex="-1" role="dialog" aria-labelledby="mutantModal" style="border: 2px green">
        <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 id="modal-title" class="modal-title">Mutated Class</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <ul>
                        <li id="file"></li>
                        <li id="mutatedMethod"></li>
                    </ul>
                    <hr>
                    <p id="mutator">Mutator</p>
                    <pre id="pre" class="line-numbers" data-start="1" data-line="6">
<code id="code" class="language-java">Code
</code>
                    </pre>
                    <hr>
                    <p id="coeffs">Coefficients</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
        integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
        crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"
        integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1"
        crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
        integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
        crossorigin="anonymous"></script>
<script type="text/javascript" src="https://cdn.datatables.net/v/bs4/dt-1.10.20/datatables.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/10.6.2/bootstrap-slider.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.19.0/components/prism-core.min.js"
        integrity="sha256-D05OTvzyl8h0SDpcQF6UNo3gl2rKDMmRuqX8ZZ06pNE=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.19.0/plugins/autoloader/prism-autoloader.min.js"
        integrity="sha256-WIuEtgHNTdrDT2obGtHYz/emxxAj04sJBdMhRjDXd8I=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.19.0/plugins/line-highlight/prism-line-highlight.min.js"
        integrity="sha256-i1I0MEGBEKKEtBLlagkEPdef5OGDQEUdcjCPYo47a3Y=" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.19.0/plugins/line-numbers/prism-line-numbers.min.js"
        integrity="sha256-hep5s8952MqR7Y79JYfCXZD6vQjVHs7sOu/ZGrs1OEQ=" crossorigin="anonymous"></script>

<script>
<#list rankers as rank>
var ${rank.getRanker()} = 0.5;  // initial value
</#list>
</script>

<script>
function updateScores() {
    var table = $('#mutants').DataTable();

    var currentRow = null;
    var newValue = 0.0;
    var highestScore = 0.0;

    // Calculate new scores
    table.cells().every( function() {
        // First column; keep reference to this cell
        if (this.node().classList.contains("score")) {
            currentRow = this.node();
            newValue = 0.0
        }
        // Add new value
        <#list rankers as rank>
        if (this.node().classList.contains("${rank.getRanker()}")) {
            newValue += ${rank.getRanker()} * parseFloat(this.node().textContent);
        }
        </#list>
        // Update score
        if (currentRow != null) {
            currentRow.setAttribute("data-score", newValue);
            if (newValue > highestScore) {
                highestScore = newValue;
            }
        }
    } );

    // Normalize scores
    table.cells().every( function() {
        if (this.node().classList.contains("score")) {
            this.node().textContent = (parseFloat(this.node().getAttribute("data-score")) / highestScore).toFixed(3);
        }
    } );

    table.rows().invalidate().draw();
}
</script>

<script>
    $('#mutantModal').on('shown.bs.modal', function (e) {
        Prism.highlightElement($('#code')[0]);  // for some reason we need to redraw to highlight a line
    });

    $("tr[data-target='#mutantModal']").on('click', function () {
        $('#code').html($(this).attr('data-location'));
        $('#pre').attr('data-line', parseInt($(this).attr('data-lineNr')));
        $('#pre').attr('data-start', parseInt($(this).attr('data-lineNr')) - 5);
        $('#file').html('<b>File</b>: <a href="' + $(this).attr('data-filepath') + '">' + $(this).attr('data-filename') + "</a>");
        $('#mutatedMethod').html("<b>Mutated Method</b>: " + $(this).attr('data-mutatedMethod'));
        $('#mutator').html($(this).attr('data-mutator'));
        $('#coeffs').html($(this).attr('data-coeffs'));
        $('#modal-title').html($(this).attr('data-mutatedClass'));
        Prism.highlightElement($('#code')[0]);
    });
</script>

<script>
    $(function () {
        $('[data-toggle="tooltip"]').tooltip()
    });
    <#list rankers as rank>
    $('#${rank.getRanker()}').slider({
        formatter: function(value) {
            return value;
        }
    });
    $("#${rank.getRanker()}").on("slideStop", function(sliderValue) {
        ${rank.getRanker()} = parseFloat(sliderValue.value);
        updateScores();
    });
    </#list>
    $(document).ready(function () {
        $('#mutants').DataTable({
            "order": [[0, "desc"]],
            "lengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]]
        });
        updateScores();
    });
</script>

</body>
</html>
