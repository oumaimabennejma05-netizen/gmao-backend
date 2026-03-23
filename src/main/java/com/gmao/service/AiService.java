package com.gmao.service;

import com.gmao.dto.AiChatResponse;
import com.gmao.dto.AiPredictionDTO;
import com.gmao.entity.Machine;
import com.gmao.entity.MachineData;
import com.gmao.exception.ResourceNotFoundException;
import com.gmao.repository.MachineDataRepository;
import com.gmao.repository.MachineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    // Seuils pour la prédiction de pannes
    private static final double TEMP_WARNING_THRESHOLD = 70.0;     // °C
    private static final double TEMP_CRITICAL_THRESHOLD = 90.0;    // °C
    private static final double VIBRATION_WARNING_THRESHOLD = 4.5; // mm/s
    private static final double VIBRATION_CRITICAL_THRESHOLD = 7.0;// mm/s
    private static final double RUNTIME_WARNING_THRESHOLD = 800.0; // heures
    private static final double RUNTIME_CRITICAL_THRESHOLD = 1200.0;// heures

    private final MachineRepository machineRepository;
    private final MachineDataRepository machineDataRepository;

    public AiService(MachineRepository machineRepository, MachineDataRepository machineDataRepository) {
        this.machineRepository = machineRepository;
        this.machineDataRepository = machineDataRepository;
    }

    public List<AiPredictionDTO> getAllPredictions() {
        List<Machine> machines = machineRepository.findAll();
        return machines.stream()
                .map(this::analyzeMachine)
                .sorted(Comparator.comparingDouble(AiPredictionDTO::getRiskScore).reversed())
                .collect(Collectors.toList());
    }

    public AiPredictionDTO getPredictionForMachine(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine", machineId));
        return analyzeMachine(machine);
    }

    private AiPredictionDTO analyzeMachine(Machine machine) {
        AiPredictionDTO prediction = new AiPredictionDTO();
        prediction.setMachineId(machine.getId());
        prediction.setMachineName(machine.getName());
        prediction.setMachineLocation(machine.getLocation());
        prediction.setAnalyzedAt(LocalDateTime.now());

        Optional<MachineData> latestDataOpt = machineDataRepository.findFirstByMachineIdOrderByCreatedAtDesc(machine.getId());

        if (latestDataOpt.isEmpty()) {
            prediction.setRiskScore(0.0);
            prediction.setRiskLevel("INCONNU");
            prediction.setAlerts(List.of("Aucune donnée télémétrique disponible pour cette machine"));
            prediction.setRecommendations(List.of("Installer des capteurs et commencer la collecte de données télémétriques de la machine"));
            return prediction;
        }

        MachineData data = latestDataOpt.get();
        prediction.setTemperature(data.getTemperature());
        prediction.setVibration(data.getVibration());
        prediction.setRuntime(data.getRuntime());

        double riskScore = calculateRiskScore(data);
        prediction.setRiskScore(riskScore);
        prediction.setRiskLevel(getRiskLevel(riskScore));
        prediction.setAlerts(generateAlerts(data, machine.getName()));
        prediction.setRecommendations(generateRecommendations(data, riskScore));

        return prediction;
    }

    private double calculateRiskScore(MachineData data) {
        double score = 0.0;
        int factors = 0;

        if (data.getTemperature() != null) {
            if (data.getTemperature() >= TEMP_CRITICAL_THRESHOLD) {
                score += 40.0;
            } else if (data.getTemperature() >= TEMP_WARNING_THRESHOLD) {
                score += 20.0 + (data.getTemperature() - TEMP_WARNING_THRESHOLD) /
                        (TEMP_CRITICAL_THRESHOLD - TEMP_WARNING_THRESHOLD) * 20.0;
            }
            factors++;
        }

        if (data.getVibration() != null) {
            if (data.getVibration() >= VIBRATION_CRITICAL_THRESHOLD) {
                score += 35.0;
            } else if (data.getVibration() >= VIBRATION_WARNING_THRESHOLD) {
                score += 15.0 + (data.getVibration() - VIBRATION_WARNING_THRESHOLD) /
                        (VIBRATION_CRITICAL_THRESHOLD - VIBRATION_WARNING_THRESHOLD) * 20.0;
            }
            factors++;
        }

        if (data.getRuntime() != null) {
            if (data.getRuntime() >= RUNTIME_CRITICAL_THRESHOLD) {
                score += 25.0;
            } else if (data.getRuntime() >= RUNTIME_WARNING_THRESHOLD) {
                score += 10.0 + (data.getRuntime() - RUNTIME_WARNING_THRESHOLD) /
                        (RUNTIME_CRITICAL_THRESHOLD - RUNTIME_WARNING_THRESHOLD) * 15.0;
            }
            factors++;
        }

        return Math.min(score, 100.0);
    }

    private String getRiskLevel(double riskScore) {
        if (riskScore >= 75) return "CRITIQUE";
        if (riskScore >= 50) return "ÉLEVÉ";
        if (riskScore >= 25) return "MOYEN";
        return "FAIBLE";
    }

    private List<String> generateAlerts(MachineData data, String machineName) {
        List<String> alerts = new ArrayList<>();

        if (data.getTemperature() != null) {
            if (data.getTemperature() >= TEMP_CRITICAL_THRESHOLD) {
                alerts.add(String.format("CRITIQUE : Température de %s à %.1f°C — arrêt immédiat recommandé !", machineName, data.getTemperature()));
            } else if (data.getTemperature() >= TEMP_WARNING_THRESHOLD) {
                alerts.add(String.format("AVERTISSEMENT : Température de %s élevée à %.1f°C — planifier une inspection du refroidissement", machineName, data.getTemperature()));
            }
        }

        if (data.getVibration() != null) {
            if (data.getVibration() >= VIBRATION_CRITICAL_THRESHOLD) {
                alerts.add(String.format("CRITIQUE : Vibration de %s à %.2f mm/s — défaillance de roulement imminente !", machineName, data.getVibration()));
            } else if (data.getVibration() >= VIBRATION_WARNING_THRESHOLD) {
                alerts.add(String.format("AVERTISSEMENT : Vibration de %s à %.2f mm/s — inspecter les roulements et supports", machineName, data.getVibration()));
            }
        }

        if (data.getRuntime() != null) {
            if (data.getRuntime() >= RUNTIME_CRITICAL_THRESHOLD) {
                alerts.add(String.format("CRITIQUE : %s a fonctionné %.0f heures — maintenance majeure en retard !", machineName, data.getRuntime()));
            } else if (data.getRuntime() >= RUNTIME_WARNING_THRESHOLD) {
                alerts.add(String.format("AVERTISSEMENT : Temps de fonctionnement de %s est de %.0f heures — maintenance préventive bientôt due", machineName, data.getRuntime()));
            }
        }

        if (alerts.isEmpty()) {
            alerts.add(machineName + " fonctionne dans les paramètres normaux");
        }

        return alerts;
    }

    private List<String> generateRecommendations(MachineData data, double riskScore) {
        List<String> recs = new ArrayList<>();

        if (data.getTemperature() != null && data.getTemperature() >= TEMP_WARNING_THRESHOLD) {
            recs.add("Vérifier et nettoyer les ventilateurs de refroidissement et les échangeurs de chaleur");
            recs.add("Vérifier les niveaux de liquide de refroidissement et la circulation");
            recs.add("Inspecter l'isolation thermique et la dissipation de chaleur");
        }

        if (data.getVibration() != null && data.getVibration() >= VIBRATION_WARNING_THRESHOLD) {
            recs.add("Effectuer une analyse vibratoire et un équilibrage");
            recs.add("Inspecter et remplacer les roulements usés si nécessaire");
            recs.add("Vérifier les boulons de fixation et l'intégrité structurelle");
        }

        if (data.getRuntime() != null && data.getRuntime() >= RUNTIME_WARNING_THRESHOLD) {
            recs.add("Planifier une révision complète de maintenance préventive");
            recs.add("Remplacer les lubrifiants et les filtres");
            recs.add("Effectuer une inspection des systèmes électriques");
        }

        if (riskScore >= 75) {
            recs.add("ACTION IMMÉDIATE : Arrêter la machine et effectuer une maintenance d'urgence");
            recs.add("Ne pas faire fonctionner tant que l'inspection n'est pas terminée et validée");
        } else if (riskScore >= 50) {
            recs.add("Planifier la maintenance dans les 48 heures");
        } else if (riskScore >= 25) {
            recs.add("Planifier la maintenance préventive dans la semaine");
        }

        if (recs.isEmpty()) {
            recs.add("Continuer la surveillance régulière");
            recs.add("Maintenir les intervalles de maintenance actuels");
        }

        return recs;
    }

    public AiChatResponse chat(String message, Long machineId) {
        String lowerMsg = message.toLowerCase();

        // Si une machine spécifique a été mentionnée
        if (machineId != null) {
            return chatAboutMachine(message, machineId);
        }

        // Questions générales
        if (lowerMsg.contains("critique") || lowerMsg.contains("danger") || lowerMsg.contains("urgence")) {
            return handleCriticalQuery();
        }
        if (lowerMsg.contains("température") || lowerMsg.contains("chaleur") || lowerMsg.contains("temp")) {
            return handleTemperatureQuery();
        }
        if (lowerMsg.contains("vibration") || lowerMsg.contains("vibrer") || lowerMsg.contains("roulement")) {
            return handleVibrationQuery();
        }
        if (lowerMsg.contains("maintenance") || lowerMsg.contains("entretien") || lowerMsg.contains("réparation")) {
            return handleMaintenanceQuery();
        }
        if (lowerMsg.contains("prédire") || lowerMsg.contains("panne") || lowerMsg.contains("risque")) {
            return handlePredictionQuery();
        }
        if (lowerMsg.contains("toutes les machines") || lowerMsg.contains("statut") || lowerMsg.contains("aperçu")) {
            return handleOverviewQuery();
        }
        if (lowerMsg.contains("temps de fonctionnement") || lowerMsg.contains("heures") || lowerMsg.contains("disponibilité")) {
            return handleRuntimeQuery();
        }

        return new AiChatResponse(
            "Je peux vous aider avec l'analyse de l'état des machines, les prédictions de pannes et les recommandations de maintenance. " +
            "Vous pouvez me poser des questions sur : les anomalies de température, les niveaux de vibration, les heures de fonctionnement, " +
            "la planification de la maintenance, les évaluations de risques, ou sélectionner une machine spécifique pour une analyse détaillée.",
            "INFO"
        );
    }

    private AiChatResponse chatAboutMachine(String message, Long machineId) {
        try {
            AiPredictionDTO prediction = getPredictionForMachine(machineId);
            StringBuilder sb = new StringBuilder();
            sb.append("Analyse pour ").append(prediction.getMachineName()).append(" :\n\n");
            sb.append("Niveau de risque : ").append(prediction.getRiskLevel())
              .append(" (Score : ").append(String.format("%.1f", prediction.getRiskScore())).append("/100)\n\n");

            if (prediction.getAlerts() != null && !prediction.getAlerts().isEmpty()) {
                sb.append("Alertes :\n");
                prediction.getAlerts().forEach(a -> sb.append("• ").append(a).append("\n"));
                sb.append("\n");
            }
            if (prediction.getRecommendations() != null && !prediction.getRecommendations().isEmpty()) {
                sb.append("Recommandations :\n");
                prediction.getRecommendations().forEach(r -> sb.append("• ").append(r).append("\n"));
            }

            String severity = prediction.getRiskLevel().equals("CRITIQUE") || prediction.getRiskLevel().equals("ÉLEVÉ")
                    ? prediction.getRiskLevel() : "INFO";
            return new AiChatResponse(sb.toString(), severity);
        } catch (Exception e) {
            return new AiChatResponse("Impossible de récupérer les données pour la machine sélectionnée.", "INFO");
        }
    }

    private AiChatResponse handleCriticalQuery() {
        List<AiPredictionDTO> predictions = getAllPredictions();
        long criticalCount = predictions.stream()
                .filter(p -> "CRITIQUE".equals(p.getRiskLevel())).count();

        if (criticalCount == 0) {
            return new AiChatResponse(
                "Bonne nouvelle ! Aucune machine n'est actuellement dans un état critique. " +
                "Il y a " + predictions.stream().filter(p -> "ÉLEVÉ".equals(p.getRiskLevel())).count() +
                " machine(s) à risque ÉLEVÉ qui nécessitent une attention prochaine.",
                "INFO"
            );
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ALERTE CRITIQUE : ").append(criticalCount).append(" machine(s) nécessitent une attention immédiate :\n\n");
        predictions.stream()
                .filter(p -> "CRITIQUE".equals(p.getRiskLevel()))
                .forEach(p -> {
                    sb.append("• ").append(p.getMachineName()).append(" (Score : ")
                      .append(String.format("%.1f", p.getRiskScore())).append(")\n");
                    if (p.getAlerts() != null && !p.getAlerts().isEmpty()) {
                        sb.append("  → ").append(p.getAlerts().get(0)).append("\n");
                    }
                });

        return new AiChatResponse(sb.toString(), "CRITIQUE");
    }

    private AiChatResponse handleTemperatureQuery() {
        List<AiPredictionDTO> predictions = getAllPredictions();
        List<AiPredictionDTO> tempIssues = predictions.stream()
                .filter(p -> p.getTemperature() != null && p.getTemperature() >= TEMP_WARNING_THRESHOLD)
                .collect(Collectors.toList());

        if (tempIssues.isEmpty()) {
            return new AiChatResponse(
                "Toutes les machines fonctionnent dans des plages de température normales (en dessous de " + TEMP_WARNING_THRESHOLD + "°C). " +
                "La température de fonctionnement normale pour la plupart des machines industrielles est de 40-70°C.",
                "INFO"
            );
        }

        StringBuilder sb = new StringBuilder();
        sb.append(tempIssues.size()).append(" machine(s) ont des températures élevées :\n\n");
        tempIssues.forEach(p -> sb.append("• ").append(p.getMachineName())
                .append(" : ").append(String.format("%.1f°C", p.getTemperature()))
                .append(p.getTemperature() >= TEMP_CRITICAL_THRESHOLD ? " [CRITIQUE]" : " [AVERTISSEMENT]")
                .append("\n"));
        sb.append("\nUne température élevée peut indiquer : une défaillance du refroidissement, une charge excessive, des problèmes de lubrification ou des problèmes de température ambiante.");

        return new AiChatResponse(sb.toString(), tempIssues.stream().anyMatch(p -> p.getTemperature() >= TEMP_CRITICAL_THRESHOLD) ? "CRITIQUE" : "AVERTISSEMENT");
    }

    private AiChatResponse handleVibrationQuery() {
        return new AiChatResponse(
            "Seuils de vibration pour les machines industrielles :\n" +
            "• Normal : < 4.5 mm/s\n• Avertissement : 4.5 - 7.0 mm/s (planifier une inspection)\n• Critique : > 7.0 mm/s (arrêter la machine immédiatement)\n\n" +
            "Une vibration élevée indique généralement : usure des roulements, déséquilibre, désalignement, pièces desserrées ou problèmes de résonance.\n\n" +
            "Recommandation : Effectuer une analyse vibratoire régulière toutes les 500 heures de fonctionnement.",
            "INFO"
        );
    }

    private AiChatResponse handleMaintenanceQuery() {
        List<AiPredictionDTO> predictions = getAllPredictions();
        long needsMaintenance = predictions.stream()
                .filter(p -> p.getRiskScore() >= 25).count();

        return new AiChatResponse(
            "Aperçu de la maintenance :\n" +
            "• Machines nécessitant une attention : " + needsMaintenance + "/" + predictions.size() + "\n" +
            "• Critique (immédiate) : " + predictions.stream().filter(p -> "CRITIQUE".equals(p.getRiskLevel())).count() + "\n" +
            "• Haute priorité (48h) : " + predictions.stream().filter(p -> "ÉLEVÉ".equals(p.getRiskLevel())).count() + "\n" +
            "• Priorité moyenne (1 semaine) : " + predictions.stream().filter(p -> "MOYEN".equals(p.getRiskLevel())).count() + "\n\n" +
            "Calendrier de maintenance préventive :\n" +
            "• Quotidien : Inspection visuelle, vérification de la lubrification\n" +
            "• Hebdomadaire : Nettoyage, inspection des filtres\n" +
            "• Mensuel : Étalonnage complet, vérification des systèmes électriques\n" +
            "• Trimestriel : Remplacement des roulements, révision majeure",
            "INFO"
        );
    }

    private AiChatResponse handlePredictionQuery() {
        List<AiPredictionDTO> predictions = getAllPredictions();
        StringBuilder sb = new StringBuilder();
        sb.append("Résumé de l'évaluation des risques IA :\n\n");

        Map<String, Long> riskCounts = predictions.stream()
                .collect(Collectors.groupingBy(AiPredictionDTO::getRiskLevel, Collectors.counting()));

        sb.append("• CRITIQUE : ").append(riskCounts.getOrDefault("CRITIQUE", 0L)).append(" machine(s)\n");
        sb.append("• ÉLEVÉ : ").append(riskCounts.getOrDefault("ÉLEVÉ", 0L)).append(" machine(s)\n");
        sb.append("• MOYEN : ").append(riskCounts.getOrDefault("MOYEN", 0L)).append(" machine(s)\n");
        sb.append("• FAIBLE : ").append(riskCounts.getOrDefault("FAIBLE", 0L)).append(" machine(s)\n");
        sb.append("• INCONNU : ").append(riskCounts.getOrDefault("INCONNU", 0L)).append(" machine(s) (pas de données)\n\n");

        if (!predictions.isEmpty()) {
            AiPredictionDTO top = predictions.get(0);
            sb.append("Risque le plus élevé : ").append(top.getMachineName())
              .append(" avec un score de ").append(String.format("%.1f", top.getRiskScore())).append("/100");
        }

        return new AiChatResponse(sb.toString(), "INFO");
    }

    private AiChatResponse handleOverviewQuery() {
        long total = machineRepository.count();
        List<AiPredictionDTO> predictions = getAllPredictions();
        long criticalCount = predictions.stream().filter(p -> "CRITIQUE".equals(p.getRiskLevel())).count();
        long highCount = predictions.stream().filter(p -> "ÉLEVÉ".equals(p.getRiskLevel())).count();

        return new AiChatResponse(
            "Aperçu du parc machines :\n" +
            "• Total machines surveillées : " + total + "\n" +
            "• Machines avec télémétrie : " + predictions.stream().filter(p -> !"INCONNU".equals(p.getRiskLevel())).count() + "\n" +
            "• Machines en état critique : " + criticalCount + "\n" +
            "• Machines à risque élevé : " + highCount + "\n\n" +
            (criticalCount > 0 ? "⚠️ ALERTE : " + criticalCount + " machine(s) nécessitent une intervention immédiate !\n" : "✅ Aucune situation critique détectée.\n") +
            "\nUtilisez la page Prédictions IA pour une analyse détaillée par machine.",
            criticalCount > 0 ? "CRITIQUE" : "INFO"
        );
    }

    private AiChatResponse handleRuntimeQuery() {
        List<AiPredictionDTO> predictions = getAllPredictions();
        List<AiPredictionDTO> highRuntime = predictions.stream()
                .filter(p -> p.getRuntime() != null && p.getRuntime() >= RUNTIME_WARNING_THRESHOLD)
                .collect(Collectors.toList());

        if (highRuntime.isEmpty()) {
            return new AiChatResponse(
                "Toutes les machines sont dans les limites de fonctionnement acceptables (en dessous de " + RUNTIME_WARNING_THRESHOLD + " heures). " +
                "Un bon entretien du parc est évident !",
                "INFO"
            );
        }

        StringBuilder sb = new StringBuilder();
        sb.append(highRuntime.size()).append(" machine(s) approchent des seuils de maintenance :\n\n");
        highRuntime.forEach(p -> sb.append("• ").append(p.getMachineName())
                .append(" : ").append(String.format("%.0f heures", p.getRuntime()))
                .append(p.getRuntime() >= RUNTIME_CRITICAL_THRESHOLD ? " [EN RETARD !]" : " [À PLANIFIER BIENTÔT]")
                .append("\n"));

        return new AiChatResponse(sb.toString(), highRuntime.stream().anyMatch(p -> p.getRuntime() >= RUNTIME_CRITICAL_THRESHOLD) ? "AVERTISSEMENT" : "INFO");
    }
}