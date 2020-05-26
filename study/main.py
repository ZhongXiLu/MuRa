import math
import os
import sys

from scipy.optimize import basinhopping, dual_annealing, differential_evolution

FEATURES = []


class BasicMutant:

    def __init__(self, coeffs, is_bug_related=False):
        self.coeffs = coeffs
        self.is_bug_related = is_bug_related


def print_results(method_name, results, verbose=False):
    ranking_score = evaluate_rankings(results)
    print("{}: {}".format(method_name, ranking_score))
    if verbose:
        for i in range(len(results)):
            print("{}: {}".format(FEATURES[i], results[i]))
        print()
        for bugReport in sorted([f for f in os.listdir(sys.argv[1]) if not f.__contains__("bug")]):
            print("\t{}: {}".format(bugReport[:-4], evaluate_ranking(mutants[bugReport[:-4]], results)))
    return ranking_score


def mutants_from_CSV(exportedFile):
    mutants = []

    with open(exportedFile[:-4] + "_bug.csv") as f:
        lines = f.readlines()

        parameters = [parameter for parameter in lines[0].split(',')]
        if not parameters[-1].strip():  # remove trailing comma
            del parameters[-1]
        global FEATURES
        FEATURES = parameters

        bug_related_mutants = []
        for line in lines[1:]:
            coeffs = []
            for i, value in enumerate(line.split(",")):
                if value.strip():
                    coeffs.append((parameters[i], float(value)))
            bug_related_mutants.append(BasicMutant(coeffs, True))

        with open(exportedFile) as f2:

            for line in f2.readlines()[1:]:
                coeffs = []
                for i, value in enumerate(line.split(",")):
                    if value.strip():
                        coeffs.append((parameters[i], float(value)))

                bug_related = False
                for m, bug_related_mutant in enumerate(bug_related_mutants):
                    is_equal = True
                    for i, coeff in enumerate(coeffs):
                        if not math.isclose(coeff[1], bug_related_mutant.coeffs[i][1]):
                            is_equal = False
                            break

                    if is_equal:
                        bug_related = True
                        del bug_related_mutants[m]
                        break

                mutants.append(BasicMutant(coeffs, bug_related))

    return mutants


def get_score_of_mutant(mutant, weights):
    score = 0.0
    for i, coeff in enumerate(mutant.coeffs):
        score += weights[i] * coeff[1]
    return score


def evaluate_ranking(mutants, weights):
    new_mutants = sorted(mutants, key=lambda m: get_score_of_mutant(m, weights), reverse=True)
    ranks = []
    for i, mutant in enumerate(new_mutants):
        if mutant.is_bug_related:
            score = get_score_of_mutant(mutant, weights)

            mutants_with_same_score = 0
            mutants_with_higher_score = i

            j = i - 1
            while True:
                j -= 1
                if j >= 0:
                    other_score = get_score_of_mutant(new_mutants[j], weights)
                    if math.isclose(score, other_score):
                        mutants_with_same_score += 1
                        mutants_with_higher_score -= 1
                        continue
                break
            j = i + 1
            while True:
                j += 1
                if j < len(new_mutants):
                    other_score = get_score_of_mutant(new_mutants[j], weights)
                    if math.isclose(score, other_score):
                        mutants_with_same_score += 1
                        continue
                break

            ranks.append(mutants_with_higher_score + (mutants_with_same_score / 2.0))

    return (sum(ranks) / len(ranks)) / len(new_mutants)


def evaluate_rankings(weights):
    ranking_scores = []

    for bugReport in [f for f in os.listdir(sys.argv[1]) if not f.__contains__("bug")]:
        ranking_scores.append(evaluate_ranking(mutants[bugReport[:-4]], weights))

    return sum(ranking_scores) / len(ranking_scores)


print("\n\n=== ", sys.argv[1])

mutants = {}
for bugReport in [f for f in os.listdir(sys.argv[1]) if not f.__contains__("bug")]:
    mutants[bugReport[:-4]] = mutants_from_CSV(sys.argv[1] + "/" + bugReport)

best_score = 1.0
best_model = None

result = basinhopping(evaluate_rankings, [0.5] * len(FEATURES), minimizer_kwargs=dict(method="COBYLA"),
                      T=5.0, stepsize=10.0, interval=20)
score = print_results("basinhopping", result.x)
if score < best_score:
    best_score = score
    best_model = result.x

result = dual_annealing(evaluate_rankings, local_search_options=dict(method="COBYLA"),
                        bounds=[(-1, 1)] * len(FEATURES))
score = print_results("dual_annealing", result.x)
if score < best_score:
    best_score = score
    best_model = result.x

result = differential_evolution(evaluate_rankings, bounds=[(-1, 1)] * len(FEATURES), workers=-1, maxiter=200)
score = print_results("differential_evolution", result.x)
if score < best_score:
    best_score = score
    best_model = result.x

print()
print_results("BEST MODEL", best_model, verbose=True)
