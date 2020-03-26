package net.petchain.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.node.MockServices;
import net.petchain.states.PetState;
import net.petchain.states.PetStateTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.petchain.utils.TestUtils.*;

/**
 * Practical exercise instructions for Contracts Part 1.
 * The objective here is to write some contract code that verifies a transaction to issue an {@link PetState}.
 * As with the {@link PetStateTest} uncomment each unit test and run them one at a time. Use the body of the tests and the
 * task description to determine how to get the tests to pass.
 */

public class PetTransferTest {
    // A pre-defined dummy command.

    public interface Commands extends CommandData {
        class DummyCommand extends TypeOnlyCommandData implements Commands {
        }
    }

    static private final MockServices ledgerServices = new MockServices(
            Arrays.asList("net.corda.training", "net.petchain.contracts")
    );

    /**
     * Must be a Transfer command
     * Hint:
     * - For the Born command we only care about the existence of it in a transaction, therefore it should extend
     * the {@link TypeOnlyCommandData} class.
     * - The command should be defined inside {@link PetContract}.
     * - We usually encapsulate our commands in an interface inside the contract class called {@link Commands} which
     * extends the {@link CommandData} interface. The Issue command itself should be defined inside the {@link Commands}
     * interface as well as implement it, for example:
     * <p>
     * public interface Commands extends CommandData {
     * class X extends TypeOnlyCommandData implements Commands{}
     * }
     * <p>
     * - We can check for the existence of any command that implements [IOUContract.Commands] by using the
     * [requireSingleCommand] function which takes a {@link Class} argument.
     * - You can use the [requireSingleCommand] function on [tx.getCommands()] to check for the existence and type of the specified command
     * in the transaction. [requireSingleCommand] requires a Class argument to identify the type of command required.
     * <p>
     * requireSingleCommand(tx.getCommands(), REQUIRED_COMMAND.class)
     */
    @Test
    public void mustIncludeTransferCommand() {
        PetState petIn = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOut = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut);
                tx.command(BOB.getPublicKey(), new Commands.DummyCommand()); // Wrong type.
                return tx.failsWith("Contract verification failed");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut);
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Requires both Input and Output states
     * <p>
     * requireThat(requirement -> {
     * requirement.using("Message when constraint fails", (boolean constraining expression));
     * // passes all cases
     * return null;
     * });
     * <p>
     * Note that the unit tests often expect contract verification failure with a specific message which should be
     * defined with your contract constraints. If not then the unit test will fail!
     * <p>
     * You can access the list of inputs via the {@link LedgerTransaction} object which is passed into
     * [PetContract.verify].
     */
    @Test
    public void mustIncludeInputAndOutputStates() {
        PetState petIn = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOut = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PetContract.PET_CONTRACT_ID, petOut); // output state only
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Pet transfer should have one input.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn); // input state only
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Pet transfer should have one output.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut);
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Requires Current and New owner signatures.
     */
    @Test
    public void transferTransactionMustHaveCurrentAndNewOwnerSignatures() {
        PetState petIn = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOut = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut); // output state only
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Transfer());
                return tx.failsWith("New owner required to sign a pet transfer.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn); // input state only
                tx.output(PetContract.PET_CONTRACT_ID, petOut); // output state only
                tx.command(Arrays.asList(CHARLIE.getPublicKey(), DUMMY.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Current owner required to sign a pet transfer.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut);
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Requires Pet Name (optional when pet Born)
     * Now we need to ensure that Owner and Breeder are the same Party in a {@link PetState} when a Pet is born.
     */
    @Test
    public void transferTransactionMustHavePetName() {
        PetState petIn = new PetState(ALICE.getParty(), "", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOut = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOutx = new PetState(BOB.getParty(), "", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOutx); // output state only
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Requires pet's name.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut);
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }
    /**
     * New owner must differ from current owner
     * Now we need to ensure that Owner and Breeder are the same Party in a {@link PetState} when a Pet is born.
     */
    @Test
    public void transferTransactionCurrentNewOwnerMustDiffer() {
        PetState petIn = new PetState(ALICE.getParty(), "", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOut = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOutx = new PetState(ALICE.getParty(), "", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOutx); // transfer to self
                tx.command(Arrays.asList(ALICE.getPublicKey(), ALICE.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Requires pet's name.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut);
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Specific fields cannot change in a Transfer: Species, Breed, Color, Gender, Birthdate
     */
    @Test
    public void transferTransactionSomeFieldsMayNotChange() {
        PetState petIn = new PetState(ALICE.getParty(), "", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOut = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOutSpecies = new PetState(BOB.getParty(), "Momo", "Feline", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOutBreed = new PetState(BOB.getParty(), "Momo", "Canine", "Bulldog", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState petOutSex = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "male", "beige", "2006-10-12", ALICE.getParty());
        PetState petOutColor = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "black", "2006-10-12", ALICE.getParty());
        PetState petOutBirth = new PetState(BOB.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2016-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOutSpecies); // attempt to change species
                tx.command(Arrays.asList(ALICE.getPublicKey(), ALICE.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Pet species type cannot change in a pet transfer.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOutBreed); // attempt to change breed
                tx.command(Arrays.asList(ALICE.getPublicKey(), ALICE.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Pet breed type cannot change in a pet transfer.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOutSex); // attempt to change gender
                tx.command(Arrays.asList(ALICE.getPublicKey(), ALICE.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Pet gender cannot change in a pet transfer.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOutColor); // attempt to change birth date
                tx.command(Arrays.asList(ALICE.getPublicKey(), ALICE.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Pet color cannot change in a pet transfer.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOutBirth); // attempt to change species
                tx.command(Arrays.asList(ALICE.getPublicKey(), ALICE.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.failsWith("Pet birth date cannot change in a pet transfer.");
            });
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, petIn);
                tx.output(PetContract.PET_CONTRACT_ID, petOut);
                tx.command(Arrays.asList(ALICE.getPublicKey(), BOB.getPublicKey()), new PetContract.Commands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }

}
