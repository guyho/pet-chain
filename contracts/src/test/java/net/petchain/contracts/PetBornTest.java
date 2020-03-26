package net.petchain.contracts;

import net.corda.testing.contracts.DummyState;
import net.corda.testing.node.MockServices;
import net.petchain.states.PetState;
import net.petchain.states.PetStateTest;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.core.contracts.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import org.junit.*;

import static net.corda.testing.node.NodeTestUtils.transaction;
import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.petchain.utils.TestUtils.*;

import net.corda.core.transactions.LedgerTransaction;

/**
 * Practical exercise instructions for Contracts Part 1.
 * The objective here is to write some contract code that verifies a transaction to issue an {@link PetState}.
 * As with the {@link PetStateTest} uncomment each unit test and run them one at a time. Use the body of the tests and the
 * task description to determine how to get the tests to pass.
 */

public class PetBornTest {
    // A pre-defined dummy command.

    public interface Commands extends CommandData {
        class DummyCommand extends TypeOnlyCommandData implements Commands {
        }
    }

    static private final MockServices ledgerServices = new MockServices(
            Arrays.asList("net.corda.training", "net.petchain.contracts")
    );

    /**
     * Task 1.
     * Recall that Commands are required to hint to the intention of the transaction as well as take a list of
     * public keys as parameters which correspond to the required signers for the transaction.
     * Commands also become more important later on when multiple actions are possible with an IOUState, e.g. Transfer
     * and Settle.
     * TODO: Add an "Issue" command to the IOUContract and check for the existence of the command in the verify function.
     * Hint:
     * - For the Issue command we only care about the existence of it in a transaction, therefore it should extend
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
    public void mustIncludeBornCommand() {
        PetState pet = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(PetContract.PET_CONTRACT_ID, pet);
//                tx.command(guy, new Commands.DummyCommand()); // Wrong type.
                tx.command(ALICE.getPublicKey(), new Commands.DummyCommand()); // Wrong type.
                return tx.failsWith("Contract verification failed");
            });
            l.transaction(tx -> {
                tx.output(PetContract.PET_CONTRACT_ID, pet);
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Task 2.
     * As previously observed, born transactions should not have any input state references. Therefore we must check to
     * ensure that no input states are included in a transaction to issue an IOU.
     * TODO: Write a contract constraint that ensures a transaction for a Pet to be born does not include any input states.
     * Hint: use a [requireThat] lambda with a constraint to inside the [PetContract.verify] function to encapsulate your
     * constraints:
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
     * [IOUContract.verify].
     */
    @Test
    public void bornTransactionMustHaveNoInputs() {
        PetState pet = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(PetContract.PET_CONTRACT_ID, new DummyState());
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                tx.output(PetContract.PET_CONTRACT_ID, pet);
                return tx.failsWith("Pet born should have zero inputs.");
            });
            l.transaction(tx -> {
                tx.output(PetContract.PET_CONTRACT_ID, pet);
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                return tx.verifies(); // As there are no input sates
            });
            return null;
        });
    }

    /**
     * Task 3.
     * Now we need to ensure that only one {@link PetState} is issued per transaction.
     * TODO: Write a contract constraint that ensures only one output state is created in a transaction.
     * Hint: Write an additional constraint within the existing [requireThat] block which you created in the previous
     * task.
     */
    @Test
    public void bornTransactionMustHaveOneOutput() {
        PetState pet = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                tx.output(PetContract.PET_CONTRACT_ID, pet); // Two outputs fails.
                tx.output(PetContract.PET_CONTRACT_ID, pet);
                return tx.failsWith("Pet born should have one output.");
            });
            l.transaction(tx -> {
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                tx.output(PetContract.PET_CONTRACT_ID, pet); // One output passes.
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Task 4.
     * Now we need to ensure that Owner and Breeder are the same Party in a {@link PetState} when a Pet is born.
     */
    @Test
    public void bornTransactionOwnerAndBreederAreSame() {
        PetState petX = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", BOB.getParty());
        PetState pet = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                tx.output(PetContract.PET_CONTRACT_ID, petX); // Owner and Breeder are different
                return tx.failsWith("Owner and Breeder must be the same when pet is born.");
            });
            l.transaction(tx -> {
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                tx.output(PetContract.PET_CONTRACT_ID, pet);
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Task 5.
     * Now we need to ensure that expected fields are included in a {@link PetState} when a Pet is born.
     */
    @Test

    public void bornTransactionMissingSpecies() {
        PetState petX = new PetState(ALICE.getParty(), "Momo", "", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        PetState pet = new PetState(ALICE.getParty(), "Momo", "Canine", "Cockapoo", "female", "beige", "2006-10-12", ALICE.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                tx.output(PetContract.PET_CONTRACT_ID, petX); // Owner and Breeder are different
                return tx.failsWith("Pet's species type, e.g., canine, feline, required when recording birth.");
            });
            l.transaction(tx -> {
                tx.command(ALICE.getPublicKey(), new PetContract.Commands.Born());
                tx.output(PetContract.PET_CONTRACT_ID, pet);
                return tx.verifies();
            });
            return null;
        });
    }

}
