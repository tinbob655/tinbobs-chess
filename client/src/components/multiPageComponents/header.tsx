import React from 'react';
import PageHeader from './pageHeader';


export default function Header():React.ReactElement {


    return (
        <React.Fragment>
            <PageHeader title="Tinbob's Chess" subtitle="Anonymously declared top #1 chess websites worldwide" />
        </React.Fragment>
    );
}