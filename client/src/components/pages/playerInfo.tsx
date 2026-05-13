import React from 'react';


interface params {
    material: number,
    blunderCount: number,
}

export default function PlayerInfo({material, blunderCount}:params):React.ReactElement {

    return (
        <React.Fragment>
            <p style={{color: 'green'}}>
                Material: {material}
            </p>
            <p style={{color: 'orange'}}>
                Blunders: {blunderCount}
            </p>
        </React.Fragment>
    );
};