import React from 'react';
import { Jelly} from 'ldrs/react';
import 'ldrs/react/Jelly.css';

interface params {
    botThinking: boolean;
}

export default function Thinking({botThinking}:params):React.ReactElement {

    return (
        <div style={{height: '50px'}}>
            {botThinking ? (
                <React.Fragment>

                    {/*bot is thinking: show loading bar*/}
                    <p>
                        Thinking  <Jelly color="#D4AF5C" size={35} />
                    </p>
                </React.Fragment>
            ) : (
                <React.Fragment>

                    {/*player's turn: tell them*/}
                    <p>
                        Your turn!
                    </p>
                </React.Fragment>
            )}
        </div>
    )
}