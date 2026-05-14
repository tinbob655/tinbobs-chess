import React from 'react';


interface params {
    material: number,
    blunderCount: number,
}

export default function PlayerInfo({material, blunderCount}:params):React.ReactElement {

    return (
        <div className="playerInfoCard">
            <div className="playerInfoStat playerInfoStat--material">
                <span className="playerInfoLabel">Material</span>
                <span className="playerInfoValue">{material}</span>
            </div>
            <div className="playerInfoStat playerInfoStat--blunders">
                <span className="playerInfoLabel">Blunders</span>
                <span className="playerInfoValue">{blunderCount}</span>
            </div>
        </div>
    );
};