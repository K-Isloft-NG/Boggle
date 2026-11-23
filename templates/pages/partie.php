<?php
declare(strict_types=1);

use Boogle\Core\Csrf;
use Boogle\Service\ReglesJeu;

$lettres = $partie['grille'];
$actives = [];
$description = 'Grille de jeu, ligne par ligne : ' . implode(', ', $lettres);
$fin = $partie['debut'] + $partie['duree_s'];
?>
<section class="partie-jeu">
    <div class="entete-page">
        <div>
            <h1>Partie en cours</h1>
            <p class="carte-soustitre">Score : <strong class="mono"><?= e($partie['score']) ?></strong></p>
        </div>
        <p class="minuteur mono" data-fin="<?= e($fin) ?>" data-instant="<?= e($fin - $secondesRestantes) ?>">
            Temps restant : <span><?= e(sprintf('%d:%02d', intdiv($secondesRestantes, 60), $secondesRestantes % 60)) ?></span>
        </p>
    </div>

    <div class="carte partie-plateau">
        <?php require RACINE . '/templates/partials/grille.php'; ?>
        <div class="partie-commandes">
            <form method="post" action="/?page=partie">
                <?= Csrf::champ() ?>
                <div class="champ">
                    <label for="mot">Votre mot</label>
                    <input type="text" id="mot" name="mot" maxlength="<?= e(ReglesJeu::LONGUEUR_MAX_SAISIE) ?>"
                           autocomplete="off" autocapitalize="characters" autofocus required>
                </div>
                <button class="bouton bouton--plein" type="submit">Proposer</button>
            </form>
            <form method="post" action="/?page=terminer-partie">
                <?= Csrf::champ() ?>
                <button class="bouton bouton--secondaire" type="submit">Terminer la partie</button>
            </form>
            <form method="post" action="/?page=abandonner-partie">
                <?= Csrf::champ() ?>
                <button class="bouton bouton--secondaire" type="submit">Abandonner sans résultat</button>
            </form>
        </div>
    </div>

    <section class="carte">
        <h2>Vos propositions</h2>
        <?php if ($partie['propositions'] === []): ?>
            <p class="carte-soustitre">Aucun mot proposé.</p>
        <?php else: ?>
            <ul>
                <?php foreach (array_reverse($partie['propositions']) as $proposition): ?>
                    <li>
                        <strong class="mono"><?= e($proposition['mot']) ?></strong> :
                        <?php if ($proposition['valide']): ?>
                            <?= e($proposition['points']) ?> <?= e($proposition['points'] === 1 ? 'point' : 'points') ?>
                        <?php else: ?>
                            <?= e($motifs[$proposition['motif_rejet']]) ?>
                        <?php endif; ?>
                    </li>
                <?php endforeach; ?>
            </ul>
        <?php endif; ?>
    </section>
</section>
<script src="/assets/js/minuteur.js" defer></script>
