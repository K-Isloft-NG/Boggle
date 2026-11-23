<?php
declare(strict_types=1);

use Boogle\Core\Csrf;

$lettres = $partie['grille'];
$actives = [];
$description = 'Grille de jeu, ligne par ligne : ' . implode(', ', $lettres);
$acceptes = [];
$refuses = [];
foreach ($partie['propositions'] as $proposition) {
    if ($proposition['valide']) {
        $acceptes[] = $proposition;
    } else {
        $refuses[] = $proposition;
    }
}

$aTrouver = array_diff($partie['solutions'], array_column($acceptes, 'mot'));
usort($aTrouver, static function (string $a, string $b): int {
    return (strlen($b) <=> strlen($a)) ?: strcmp($a, $b);
});
?>
<section class="partie-jeu">
    <div class="entete-page">
        <div>
            <h1>Partie terminée</h1>
            <p class="carte-soustitre">
                Score final : <strong class="mono"><?= e($partie['score']) ?></strong>
                · <?= e(count($acceptes)) ?> <?= e(count($acceptes) === 1 ? 'mot trouvé' : 'mots trouvés') ?>
            </p>
        </div>
        <form method="post" action="/?page=nouvelle-partie">
            <?= Csrf::champ() ?>
            <button class="bouton bouton--plein" type="submit">Nouvelle partie</button>
        </form>
    </div>

    <div class="carte partie-plateau">
        <?php require RACINE . '/templates/partials/grille.php'; ?>
        <div>
            <h2>Mots acceptés</h2>
            <?php if ($acceptes === []): ?>
                <p class="carte-soustitre">Aucun mot accepté.</p>
            <?php else: ?>
                <ul>
                    <?php foreach ($acceptes as $proposition): ?>
                        <li>
                            <strong class="mono"><?= e($proposition['mot']) ?></strong> :
                            <?= e($proposition['points']) ?> <?= e($proposition['points'] === 1 ? 'point' : 'points') ?>
                        </li>
                    <?php endforeach; ?>
                </ul>
            <?php endif; ?>
            <h2>Mots refusés</h2>
            <?php if ($refuses === []): ?>
                <p class="carte-soustitre">Aucun mot refusé.</p>
            <?php else: ?>
                <ul>
                    <?php foreach ($refuses as $proposition): ?>
                        <li>
                            <strong class="mono"><?= e($proposition['mot']) ?></strong> :
                            <?= e($motifs[$proposition['motif_rejet']]) ?>
                        </li>
                    <?php endforeach; ?>
                </ul>
            <?php endif; ?>
        </div>
    </div>

    <section class="carte">
        <h2>Mots que vous auriez pu trouver</h2>
        <p class="carte-soustitre">
            <?= e(count($aTrouver)) ?> <?= e(count($aTrouver) === 1 ? 'mot possible' : 'mots possibles') ?>
            <?php if (count($aTrouver) > 60): ?>· 60 affichés<?php endif; ?>
        </p>
        <?php if ($aTrouver !== []): ?>
            <ul class="partie-solutions">
                <?php foreach (array_slice($aTrouver, 0, 60) as $mot): ?>
                    <li class="mono"><?= e($mot) ?></li>
                <?php endforeach; ?>
            </ul>
        <?php endif; ?>
    </section>
    <a class="lien" href="/?page=parties">Retour à mes parties</a>
</section>
