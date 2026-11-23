<?php
declare(strict_types=1);

namespace Boogle\Repository;

use DateTimeImmutable;
use DateTimeZone;
use PDO;
use RuntimeException;

final class PartieRepository
{
    public function __construct(private readonly PDO $pdo)
    {
    }

    public function identifiantDictionnaire(string $langue): ?int
    {
        $requete = $this->pdo->prepare(
            'SELECT id_dictionnaire FROM boggle.dictionnaire WHERE code_langue = :langue ORDER BY version DESC LIMIT 1'
        );
        $requete->execute([':langue' => $langue]);
        $identifiant = $requete->fetchColumn();

        return $identifiant === false ? null : (int) $identifiant;
    }

    public function enregistrer(
        int $idDictionnaire,
        string $statut,
        DateTimeImmutable $debut,
        DateTimeImmutable $fin,
        string $donneesJson
    ): int {
        $utc = new DateTimeZone('UTC');
        $requete = $this->pdo->prepare(
            'SELECT boggle.enregistrer_partie(:dictionnaire, :statut, :debut, :fin, CAST(:donnees AS jsonb)) AS id_partie'
        );
        $requete->bindValue(':dictionnaire', $idDictionnaire, PDO::PARAM_INT);
        $requete->bindValue(':statut', $statut, PDO::PARAM_STR);
        $requete->bindValue(':debut', $debut->setTimezone($utc)->format(DATE_ATOM), PDO::PARAM_STR);
        $requete->bindValue(':fin', $fin->setTimezone($utc)->format(DATE_ATOM), PDO::PARAM_STR);
        $requete->bindValue(':donnees', $donneesJson, PDO::PARAM_STR);
        $requete->execute();

        $identifiants = $requete->fetchAll(PDO::FETCH_COLUMN);
        if (count($identifiants) !== 1 || $identifiants[0] === null) {
            throw new RuntimeException('Identifiant de partie manquant.');
        }

        return (int) $identifiants[0];
    }
}
