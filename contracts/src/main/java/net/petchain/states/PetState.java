package net.petchain.states;

import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.petchain.contracts.PetContract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@BelongsToContract(PetContract.class)
public class PetState implements ContractState {

    private final Party owner;
    private final String petName;
    private final String species;
    private final String breed;
    private final String sex;
    private final String color;
    private final String birthDate;
    private final Party breeder;
//    private String birthPlace;
//    private Date deathDate;
//    private String deathPlace;
//    private String deathCause;
//    private final String rabiesLicense;
//    private String rabiesIssuer;
//    private final String rabiesIssueDate;
//    private final String rabiesExpireDate;
//    private final String muniLicense;

    public PetState (Party owner, String petName, String species, String breed, String sex, String color, String birthDate, Party breeder) {
        this.owner = owner;
        this.petName = petName;
        this.species = species;
        this.breed = breed;
        this.sex = sex;
        this.color = color;
        this.birthDate = birthDate;

        this.breeder = breeder;
//        this.birthPlace = birthPlace;
//        this.deathDate = deathDate;
//        this.deathPlace = deathPlace;
//        this.deathCause = deathCause;
//        this.rabiesLicense = rabiesLicense;
//        this.rabiesIssuer = rabiesIssuer;
//        this.rabiesIssueDate = rabiesIssueDate;
//        this.rabiesExpireDate = rabiesExpireDate;
    }

    public Party getOwner() {
        return owner;
    }

    public String getPetName() {
        return petName;
    }

    public String getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public String getSex() {
        return sex;
    }

    public String getColor() {
        return color;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public Party getBreeder() { return breeder; }

//    public String getBirthPlace() {
//        return birthPlace;
//    }
//
//    public Date getDeathDate() {
//        return deathDate;
//    }
//
//    public String getDeathPlace() {
//        return deathPlace;
//    }
//
//    public String getDeathCause() {
//        return deathCause;
//    }
//
//    public String getRabiesLicense() {
//        return rabiesLicense;
//    }
//
//    public String getRabiesIssuer() {
//        return rabiesIssuer;
//    }
//
//    public String getRabiesIssueDate() {
//        return rabiesIssueDate;
//    }
//
//    public String getRabiesExpireDate() {
//        return rabiesExpireDate;
//    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> participants = new ArrayList<>();
        participants.add(owner);
        return participants;
    }
}
