<?php
declare(strict_types=1);

namespace Boogle\Service;

/**
 * Règles de jeu appliquées par le serveur.
 *
 * La longueur minimale est définie ici et nulle part ailleurs : c'est la valeur transmise au
 * paramètre <min> de l'exécutable solve, et celle contrôlée avant tout appel à dictionnary_lookup
 * ou grid_path. Le moteur ne filtre pas lui-même les mots trop courts.
 */
final class ReglesJeu
{
    /** Longueur minimale d'un mot, comptée en lettres. */
    public const LONGUEUR_MINIMALE_MOT = 3;

    /** Nombre de lignes de la grille. */
    public const HAUTEUR_GRILLE = 4;

    /** Nombre de colonnes de la grille. */
    public const LARGEUR_GRILLE = 4;

    /** Durée d'une partie, en secondes. */
    public const DUREE_PARTIE_S = 180;

    /** Nombre maximal de lettres pour 16 cases de deux lettres. */
    public const LONGUEUR_MAX_SAISIE = 32;

    /** Borne les appels au moteur au cours d'une partie. */
    public const PROPOSITIONS_MAX = 200;

    /**
     * Lettres accentuées ramenées à A-Z, comme le fait le module Java pour le dictionnaire
     * (marques diacritiques retirées, ligatures Œ et Æ développées).
     */
    private const SANS_ACCENT = [
        'À' => 'A', 'Á' => 'A', 'Â' => 'A', 'Ã' => 'A', 'Ä' => 'A', 'Å' => 'A',
        'Ç' => 'C',
        'È' => 'E', 'É' => 'E', 'Ê' => 'E', 'Ë' => 'E',
        'Ì' => 'I', 'Í' => 'I', 'Î' => 'I', 'Ï' => 'I',
        'Ñ' => 'N',
        'Ò' => 'O', 'Ó' => 'O', 'Ô' => 'O', 'Õ' => 'O', 'Ö' => 'O',
        'Ù' => 'U', 'Ú' => 'U', 'Û' => 'U', 'Ü' => 'U',
        'Ý' => 'Y', 'Ÿ' => 'Y',
        'Œ' => 'OE', 'Æ' => 'AE',
    ];

    /**
     * Forme transmise au moteur : espaces retirés, majuscules, sans accent (ÉTÉ devient ETE).
     * Le dictionnaire et le moteur C ne connaissent que les lettres de A à Z.
     */
    public static function normaliserMot(string $mot): string
    {
        return strtr(mb_strtoupper(trim($mot), 'UTF-8'), self::SANS_ACCENT);
    }

    /**
     * Longueur du mot normalisé en lettres, comme le fait solve : QU occupe une seule case de la
     * grille mais compte pour deux lettres (QUE fait 3 lettres).
     */
    public static function longueurMot(string $mot): int
    {
        return mb_strlen(self::normaliserMot($mot), 'UTF-8');
    }

    /** Retourne un message destiné au joueur si le mot est trop court, sinon null. */
    public static function verifierLongueur(string $mot): ?string
    {
        if (self::longueurMot($mot) < self::LONGUEUR_MINIMALE_MOT) {
            return sprintf('Un mot doit contenir au moins %d lettres.', self::LONGUEUR_MINIMALE_MOT);
        }

        return null;
    }
}
