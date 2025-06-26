package tn.esprit.spring.kaddem.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.spring.kaddem.entities.Contrat;
import tn.esprit.spring.kaddem.entities.Equipe;
import tn.esprit.spring.kaddem.entities.Etudiant;
import tn.esprit.spring.kaddem.entities.Niveau;
import tn.esprit.spring.kaddem.repositories.EquipeRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class EquipeServiceImpl implements IEquipeService{
	EquipeRepository equipeRepository;

	public List<Equipe> retrieveAllEquipes(){
		log.info("Entering retrieveAllEquipes method");
		try {
			List<Equipe> equipes = (List<Equipe>) equipeRepository.findAll();
			log.debug("Retrieved {} equipes", equipes.size());
			return equipes;
		} catch (Exception e) {
			log.error("Error while retrieving all equipes: {}", e.getMessage(), e);
			throw e;
		} finally {
			log.info("Exiting retrieveAllEquipes method");
		}
	}

	public Equipe addEquipe(Equipe e){
		log.info("Adding new equipe: {}", e.getNomEquipe());
		try {
			Equipe savedEquipe = equipeRepository.save(e);
			log.debug("Equipe saved successfully with ID: {}", savedEquipe.getIdEquipe());
			return savedEquipe;
		} catch (Exception ex) {
			log.error("Error while adding equipe: {}", ex.getMessage(), ex);
			throw ex;
		}
	}

	public void deleteEquipe(Integer idEquipe){
		log.info("Deleting equipe with ID: {}", idEquipe);
		try {
			Equipe e = retrieveEquipe(idEquipe);
			equipeRepository.delete(e);
			log.debug("Equipe with ID {} deleted successfully", idEquipe);
		} catch (Exception ex) {
			log.error("Error while deleting equipe with ID {}: {}", idEquipe, ex.getMessage(), ex);
			throw ex;
		}
	}

	public Equipe retrieveEquipe(Integer equipeId){
		log.info("Retrieving equipe with ID: {}", equipeId);
		try {
			Equipe equipe = equipeRepository.findById(equipeId)
					.orElseThrow(() -> new RuntimeException("Equipe not found"));
			log.debug("Retrieved equipe: {}", equipe);
			return equipe;
		} catch (Exception e) {
			log.error("Error while retrieving equipe with ID {}: {}", equipeId, e.getMessage(), e);
			throw e;
		}
	}

	public Equipe updateEquipe(Equipe e){
		log.info("Updating equipe with ID: {}", e.getIdEquipe());
		try {
			Equipe updatedEquipe = equipeRepository.save(e);
			log.debug("Equipe updated successfully: {}", updatedEquipe);
			return updatedEquipe;
		} catch (Exception ex) {
			log.error("Error while updating equipe: {}", ex.getMessage(), ex);
			throw ex;
		}
	}

	public void evoluerEquipes(){
		log.info("Starting evoluerEquipes process");
		List<Equipe> equipes = (List<Equipe>) equipeRepository.findAll();
		log.debug("Processing {} equipes for evolution", equipes.size());

		for (Equipe equipe : equipes) {
			try {
				log.debug("Processing equipe ID: {}, Name: {}, Current Level: {}",
						equipe.getIdEquipe(), equipe.getNomEquipe(), equipe.getNiveau());

				if ((equipe.getNiveau().equals(Niveau.JUNIOR)) || (equipe.getNiveau().equals(Niveau.SENIOR))) {
					List<Etudiant> etudiants = (List<Etudiant>) equipe.getEtudiants();
					log.debug("Equipe has {} etudiants", etudiants.size());

					Integer nbEtudiantsAvecContratsActifs=0;
					for (Etudiant etudiant : etudiants) {
						Set<Contrat> contrats = etudiant.getContrats();
						for (Contrat contrat : contrats) {
							Date dateSysteme = new Date();
							long difference_In_Time = dateSysteme.getTime() - contrat.getDateFinContrat().getTime();
							long difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
							if ((contrat.getArchive() == false) && (difference_In_Years > 1)) {
								nbEtudiantsAvecContratsActifs++;
								log.trace("Found active contract for etudiant ID: {}", etudiant.getIdEtudiant());
								break;
							}
							if (nbEtudiantsAvecContratsActifs >= 3) break;
						}
					}

					if (nbEtudiantsAvecContratsActifs >= 3){
						Niveau oldNiveau = equipe.getNiveau();
						if (equipe.getNiveau().equals(Niveau.JUNIOR)){
							equipe.setNiveau(Niveau.SENIOR);
							equipeRepository.save(equipe);
							log.info("Equipe ID {} promoted from JUNIOR to SENIOR", equipe.getIdEquipe());
						} else if (equipe.getNiveau().equals(Niveau.SENIOR)){
							equipe.setNiveau(Niveau.EXPERT);
							equipeRepository.save(equipe);
							log.info("Equipe ID {} promoted from SENIOR to EXPERT", equipe.getIdEquipe());
						}
					} else {
						log.debug("Equipe ID {} doesn't meet promotion criteria (only {} etudiants with active contracts)",
								equipe.getIdEquipe(), nbEtudiantsAvecContratsActifs);
					}
				}
			} catch (Exception e) {
				log.error("Error while processing equipe ID {}: {}", equipe.getIdEquipe(), e.getMessage(), e);
			}
		}
		log.info("Finished evoluerEquipes process");
	}
}
