package net.petchain.contracts;

import net.corda.core.contracts.*;
import net.petchain.states.PetState;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

@LegalProseReference(uri = "<prose_contract_uri>")
public class PetContract implements Contract {
    public static final String PET_CONTRACT_ID = "net.petchain.contracts.PetContract";

    public interface Commands extends CommandData {
        class Born extends TypeOnlyCommandData implements Commands { }
        class Transfer extends TypeOnlyCommandData implements Commands { }
//        class Vaccinate extends TypeOnlyCommandData implements Commands { }
//        class License extends TypeOnlyCommandData implements Commands { }
    }

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
//        if(tx.getCommands().size()!= 1)
//            throw new IllegalArgumentException("Transaction must have only one command.");
//        CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        // We can use the requireSingleCommand function to extract command data from transaction.
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();

        /**
         * This command data can then be used inside of a conditional statement to indicate which set of tests we
         * should be performing - we will use different assertions to enable the contract to verify the transaction
         * for issuing, settling and transferring.
         */

        List<PublicKey> requiredSigners = command.getSigners();

// Think in terms of:
//  1) Scope constraints - nbr of inputs/outputs, nbr of commands, command types
//  2) Content constraints - inspecting inputs and outputs for expected value ranges.
//  3) Signer constraints - who should be signing on each command.

        if (commandData.equals (new Commands.Born())) {

            requireThat(require -> {

                //      Scope constraints
                require.using("Pet born should have zero inputs.", tx.getInputStates().size() == 0);
                require.using("Pet born should have one output.", tx.getOutputStates().size() == 1);
                require.using("Pet born output should be a PetState.", tx.outputsOfType(PetState.class).size() == 1);

                //      Content constraints
                ContractState outputState = tx.getOutput(0);
                PetState petStateOutput = (PetState) outputState;
                require.using("Owner and Breeder must be the same when pet is born.", petStateOutput.getOwner().equals(petStateOutput.getBreeder()));
                require.using("Pet's species type, e.g., canine, feline, required when recording birth.", !petStateOutput.getSpecies().isEmpty());
                require.using("Pet's breed type, e.g., Bulldog, Cockapoo, required when recording birth.",!petStateOutput.getBreed().isEmpty());
                require.using("Pet's color required when recording birth.", !petStateOutput.getColor().isEmpty());
                require.using("Pet's gender required when recording birth.", !petStateOutput.getSex().isEmpty());
                require.using("Pet's birth date required when recording birth.",!petStateOutput.getBirthDate().isEmpty());

                //      Signer constraints
                require.using("Pet born should have output owner's as a required signer.", requiredSigners.contains(petStateOutput.getOwner().getOwningKey()));

                return null;
                    });
//            if (petStateOutput.getPetName().isEmpty())
//                throw new IllegalArgumentException("Requires pet's name.");
//            if (petStateOutput.getOwner() != petStateOutput.getBreeder())
//                throw new IllegalArgumentException("Owner and Breeder must be the same when pet is born.");
//            if (petStateOutput.getSpecies().isEmpty())
//                throw new IllegalArgumentException("Pet's species type, e.g., canine, feline, required when recording birth.");
//            if (petStateOutput.getBreed().isEmpty())
//                throw new IllegalArgumentException("Pet's breed type, e.g., Bulldog, Cockapoo, required when recording birth.");
//            if (petStateOutput.getColor().isEmpty())
//                throw new IllegalArgumentException("Pet's color required when recording birth.");
//            if (petStateOutput.getSex().isEmpty())
//                throw new IllegalArgumentException("Pet's gender required when recording birth.");
//            if (petStateOutput.getBirthDate().isEmpty())
//                throw new IllegalArgumentException("Pet's birth date required when recording birth.");
//      Signer constraints
//            if (!(requiredSigners.contains(petStateOutput.getOwner().getOwningKey())))
//                throw new IllegalArgumentException("Pet born should have output owner's as a required signer.");

        } else if (command.getValue() instanceof Commands.Transfer) {
            requireThat(require -> {

                //      Scope constraints
                require.using("Pet transfer should have one input.", tx.getInputStates().size() == 1);
                require.using("Pet transfer should have one output.",tx.getOutputStates().size() == 1 );
                require.using("Pet transfer input should be an PetState.", tx.inputsOfType(PetState.class).size() == 1);
                require.using("Pet transfer output should be an PetState.", tx.outputsOfType(PetState.class).size() == 1);

            //      Content constraints

                ContractState inputState = tx.getInput(0);
                ContractState outputState = tx.getOutput(0);
                PetState petStateInput = (PetState) inputState;
                PetState petStateOutput = (PetState) outputState;

                require.using("Requires pet's name.", !petStateOutput.getPetName().isEmpty());
                require.using("Output Owner must differ from Input Owner in a pet transfer.", !petStateOutput.getOwner().equals(petStateInput.getOwner()));
                require.using("Pet species type cannot change in a pet transfer.", petStateOutput.getSpecies().equals(petStateInput.getSpecies()));
                require.using("Pet breed type cannot change in a pet transfer.", petStateOutput.getBreed().equals(petStateInput.getBreed()));
                require.using("Pet color cannot change in a pet transfer.", petStateOutput.getColor().equals(petStateInput.getColor()));
                require.using("Pet gender cannot change in a pet transfer.", petStateOutput.getSex().equals(petStateInput.getSex()));
                require.using("Pet birth date cannot change in a pet transfer.", petStateOutput.getBirthDate().equals(petStateInput.getBirthDate()));

                //      Signer constraints

                require.using("Current owner required to sign a pet transfer.", requiredSigners.contains(petStateInput.getOwner().getOwningKey()));
                require.using("New owner required to sign a pet transfer.", requiredSigners.contains(petStateOutput.getOwner().getOwningKey()));
//            if (petStateOutput.getPetName().isEmpty())
//                throw new IllegalArgumentException("Requires pet's name.");
//            if (petStateOutput.getOwner().equals(petStateInput.getOwner()))
//                throw new IllegalArgumentException("Output Owner must differ from Input Owner in a pet transfer.");
//            if (!petStateOutput.getSpecies().equals(petStateInput.getSpecies()))
//                throw new IllegalArgumentException("Pet's species type cannot change in a pet Transfer");
//            if (!petStateOutput.getBreed().equals(petStateInput.getBreed()))
//                throw new IllegalArgumentException("Pet's breed type cannot change in a pet transfer");
//            if (!petStateOutput.getColor().equals(petStateInput.getColor()))
//                throw new IllegalArgumentException("Pet's color cannot change in a pet transfer");
//            if (!petStateOutput.getSex().equals(petStateInput.getSex()))
//                throw new IllegalArgumentException("Pet's gender type cannot change in a pet transfer");
//            if (!petStateOutput.getBirthDate().equals(petStateInput.getBirthDate()))
//                throw new IllegalArgumentException("Pet's birth date cannot change in a pet transfer");

            //      Signer constraints
//            if (!(requiredSigners.contains(petStateInput.getOwner().getOwningKey())))
//                throw new IllegalArgumentException("Current owner required to sign a pet transfer.");
//            if (!(requiredSigners.contains(petStateOutput.getOwner().getOwningKey())))
//                throw new IllegalArgumentException("New owner required to sign a pet transfer.");

                return null;
            });

        } else throw new IllegalArgumentException("Unrecognized command!");
    }
}
