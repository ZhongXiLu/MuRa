<!DOCTYPE html>
<html lang="en" style="height: 100%">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/bs4/dt-1.10.20/datatables.min.css"/>

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
                <br>
                <#list rankers as rank>
                    <label for="customRange1"><h5>${rank.getRanker()}</h5></label>
                    <input type="range" class="custom-range" id="${rank.getRanker()}">
                </#list>
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
                    <tr <#if mutant.survived()> class="table-success" <#else> class="table-danger" </#if>>
                        <td><b>${mutant.getScore()}</b></td>

                        <#list mutant.getRankCoefficients() as rank>
                            <td data-toggle="tooltip" data-delay="500" data-placement="top"
                                title="${rank.getExplanation()}">${rank.getValue()}</td>
                        </#list>

                        <#if mutant.mutatedMethod == "<init>">
                            <td><samp>${mutant.mutatedClass}.<b>&lt;init&gt;</b><i>${mutant.mutatedMethodDescr}</i></samp></td>
                        <#else>
                            <td><samp>${mutant.mutatedClass}.<b>${mutant.mutatedMethod}</b><i>${mutant.mutatedMethodDescr}</i></samp></td>
                        </#if>
                        <td data-toggle="tooltip" data-delay="500" data-placement="top"
                            title="${mutant.notes}">${mutant.mutator}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
            <br><br>
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

<script>
    $(function () {
        $('[data-toggle="tooltip"]').tooltip()
    });
    $(document).ready(function () {
        $('#mutants').DataTable({
            "order": [[0, "desc"]],
            "lengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
            "pagingType": "numbers"
        });
    });
</script>

</body>
</html>
