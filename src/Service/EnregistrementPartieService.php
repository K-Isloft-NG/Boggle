<?php
declare(strict_types=1);

namespace Boogle\Service;

use Boogle\Repository\PartieRepository;
use Closure;
use DateTimeImmutable;
use RuntimeException;

final class EnregistrementPartieService
{
    /** Le dépôt peut être créé à la demande pour différer l'ouverture de la connexion. */
    public function __construct(private readonly PartieRepository|Closure $depot)
    {
    }

    public function construireDonnees(array $partie, int $idUtilisateur, ?int $abandonTMs): string
    {
        $joueur = [
            'id_utilisateur' => $idUtilisateur,
            'propositions'   => array_values($partie['propositions']),
            'score'          => $partie['score'],
        ];
        if ($abandonTMs !== null) {
            $joueur['abandon_t_ms'] = $abandonTMs;
        }

        return json_encode([
            'version_format' => ReglesJeu::VERSION_FORMAT_PARTIE,
            'config' => [
                'lignes'         => $partie['hauteur'],
                'colonnes'       => $partie['largeur'],
                'duree_s'        => $partie['duree_s'],
                'taille_min_mot' => ReglesJeu::LONGUEUR_MINIMALE_MOT,
                'politique_score' => ReglesJeu::POLITIQUE_SCORE,
            ],
            'grille' => array_values($partie['grille']),
            'joueurs' => [$joueur],
        ], JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
    }

    public function enregistrer(array $partie, int $idUtilisateur, string $statut, ?int $abandonTMs = null): int
    {
        $depot = $this->depot instanceof Closure ? ($this->depot)() : $this->depot;
        $idDictionnaire = $depot->identifiantDictionnaire(ReglesJeu::LANGUE_DICTIONNAIRE);
        if ($idDictionnaire === null) {
            throw new RuntimeException('Dictionnaire introuvable.');
        }

        $debut = $partie['debut'];
        $fin = max($debut, min(microtime(true), $debut + $partie['duree_s']));

        return $depot->enregistrer(
            $idDictionnaire,
            $statut,
            $this->instantUtc($debut),
            $this->instantUtc($fin),
            $this->construireDonnees($partie, $idUtilisateur, $abandonTMs)
        );
    }

    private function instantUtc(float $instant): DateTimeImmutable
    {
        $date = DateTimeImmutable::createFromFormat('U.u', sprintf('%.6F', $instant));
        if ($date === false) {
            throw new RuntimeException('Date de partie invalide.');
        }

        return $date;
    }
}
