<?php
declare(strict_types=1);

namespace Boogle\Controller;

use Boogle\Core\Config;
use Boogle\Core\Database;
use Boogle\Core\Router;
use Boogle\Core\Session;
use Boogle\Core\View;
use Boogle\Repository\PartieRepository;
use Boogle\Service\EnregistrementPartieService;
use Boogle\Service\Moteur;
use Boogle\Service\MoteurException;
use Boogle\Service\PartieService;
use InvalidArgumentException;

/**
 * Partie solo en session et liste de démonstration.
 * Le paramètre etat=vide affiche la démonstration de l'état sans partie.
 */
final class PartieController
{
    private const PARTIES_DEMO = [
        ['date' => '09/11/2026', 'adversaires' => 'Marie',         'score' => 12, 'mots' => 5,  'statut' => 'En cours'],
        ['date' => '08/11/2026', 'adversaires' => 'Marie, Paul',   'score' => 42, 'mots' => 14, 'statut' => 'Terminée'],
        ['date' => '05/11/2026', 'adversaires' => 'Jean',          'score' => 37, 'mots' => 12, 'statut' => 'Terminée'],
        ['date' => '02/11/2026', 'adversaires' => 'Marie, Camille', 'score' => 18, 'mots' => 7,  'statut' => 'Abandonnée'],
        ['date' => '29/10/2026', 'adversaires' => 'Paul',          'score' => 55, 'mots' => 19, 'statut' => 'Terminée'],
    ];

    private const MOTIFS = [
        'trop_court'         => 'trop court',
        'doublon'           => 'déjà proposé',
        'absent_grille'     => 'absent de la grille',
        'absent_dictionnaire' => 'absent du dictionnaire',
    ];

    private readonly PartieService $service;

    public function __construct()
    {
        $moteur = new Moteur(
            Config::obtenir('MOTEUR_BIN', '/opt/boogle/moteur/bin'),
            Config::obtenir('MOTEUR_DICO', '/opt/boogle/donnees/dico.lex'),
            Config::obtenir('MOTEUR_FREQUENCES', '/opt/boogle/donnees/chartable.txt')
        );
        $enregistrement = new EnregistrementPartieService(
            static fn (): PartieRepository => new PartieRepository(Database::connexion())
        );
        $this->service = new PartieService($moteur, $enregistrement);
    }

    public function liste(): void
    {
        $etatVide = ($_GET['etat'] ?? '') === 'vide';

        View::rendre('parties', [
            'titre'    => 'Vos parties',
            'parties'  => $etatVide ? [] : self::PARTIES_DEMO,
            'etatVide' => $etatVide,
        ]);
    }

    public function nouvelle(): void
    {
        try {
            $this->service->demarrer();
        } catch (MoteurException $erreur) {
            $this->signalerErreurMoteur($erreur);
            Router::rediriger('parties');
        }

        Router::rediriger('partie');
    }

    public function afficher(): void
    {
        try {
            $partie = $this->service->partieEnCours();
        } catch (MoteurException $erreur) {
            $this->signalerErreurMoteur($erreur);
            Router::rediriger('partie');
        }

        if ($partie === null) {
            Session::ajouterMessage('info', 'Lancez une nouvelle partie pour jouer.');
            Router::rediriger('parties');
        }

        if ($partie['terminee']) {
            View::rendre('resultat', ['titre' => 'Partie terminée', 'partie' => $partie, 'motifs' => self::MOTIFS]);
            return;
        }

        View::rendre('partie', [
            'titre'   => 'Partie en cours',
            'partie'  => $partie,
            'motifs'  => self::MOTIFS,
            'secondesRestantes' => $this->service->secondesRestantes(),
        ]);
    }

    public function proposer(): void
    {
        $saisie = $_POST['mot'] ?? '';
        try {
            $proposition = $this->service->proposer(is_string($saisie) ? $saisie : '');
            $mot = $proposition['mot'];
            if ($proposition['valide']) {
                $points = $proposition['points'];
                $unite = $points === 1 ? 'point' : 'points';
                Session::ajouterMessage('succes', "$mot : $points $unite");
            } else {
                Session::ajouterMessage('erreur', $mot . ' : ' . self::MOTIFS[$proposition['motif_rejet']]);
            }
        } catch (InvalidArgumentException $erreur) {
            Session::ajouterMessage('erreur', $erreur->getMessage());
        } catch (MoteurException $erreur) {
            $this->signalerErreurMoteur($erreur);
        }

        Router::rediriger('partie');
    }

    public function terminer(): void
    {
        try {
            $this->service->terminer();
        } catch (MoteurException $erreur) {
            $this->signalerErreurMoteur($erreur);
        }

        Router::rediriger('partie');
    }

    public function abandonner(): void
    {
        $this->service->abandonner();
        $message = $this->service->abandonNonEnregistre()
            ? "Partie abandonnée, mais elle n'a pas pu être enregistrée."
            : 'Partie abandonnée.';
        Session::ajouterMessage('info', $message);
        Router::rediriger('parties');
    }

    private function signalerErreurMoteur(MoteurException $erreur): void
    {
        error_log($erreur->getMessage());
        Session::ajouterMessage('erreur', 'Le moteur de jeu est indisponible pour le moment.');
    }
}
