package net.petchain.states;

import net.petchain.states.PetState;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PetStateTest {
    private final Party jan = new TestIdentity(new CordaX500Name("Jan's Cockapoos", "West Orange", "US")).getParty();

    @Test
    public void petStateHasOwnerEtcOfCorrectTypeInConstructor() {
        new PetState(jan,"Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", jan);
    }

    @Test
    public void petStateHasGettersForOwnerEtc() {
        PetState petState = new PetState(jan, "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", jan);
        assertEquals(jan, petState.getOwner());
        assertEquals("Momo", petState.getPetName());
        assertEquals("Canine", petState.getSpecies());
        assertEquals("Cockapoo", petState.getBreed());
        assertEquals("female", petState.getSex());
        assertEquals("beige", petState.getColor());
        assertEquals("2006-10-12", petState.getBirthDate());
        assertEquals(jan, petState.getBreeder());
//        assertEquals("ABC000", petState.getRabiesLicense());
//        assertEquals("2019-09-01", petState.getRabiesIssueDate());
//        assertEquals("2021-08-31", petState.getRabiesExpireDate());
    }

    @Test
    public void petStateImplementsContractState() {
        assertTrue(new PetState(jan, "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", jan) instanceof ContractState);
    }

    @Test
    public void petStateHasOneParticipantTheOwner() {
        PetState petState = new PetState(jan, "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", jan);
        assertEquals(1, petState.getParticipants().size());
        assertTrue(petState.getParticipants().contains(jan));
    }
}
